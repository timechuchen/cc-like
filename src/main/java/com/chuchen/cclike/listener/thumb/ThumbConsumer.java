package com.chuchen.cclike.listener.thumb;

import cn.hutool.core.lang.Pair;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chuchen.cclike.listener.thumb.msg.ThumbEvent;
import com.chuchen.cclike.mapper.BlogMapper;
import com.chuchen.cclike.model.entity.Thumb;
import com.chuchen.cclike.service.ThumbService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.api.SubscriptionType;
import org.apache.pulsar.common.schema.SchemaType;
import org.springframework.pulsar.annotation.PulsarListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ThumbConsumer {  
  
    private final BlogMapper blogMapper;
    private final ThumbService thumbService;

    @PulsarListener(topics = "thumb-dlq-topic")
    public void consumerDlq(Message<ThumbEvent> message) {
        MessageId messageId = message.getMessageId();
        log.info("dlq message = {}", messageId);
        log.info("消息 {} 已入库", messageId);
        log.info("已通知相关人员 {} 处理消息 {}", "初晨", messageId);
    }
    // 批量处理配置  
    @PulsarListener(
            subscriptionName = "thumb-subscription",  
            topics = "thumb-topic",  
            schemaType = SchemaType.JSON,
            batch = true,  
            //consumerCustomizer = "thumbConsumerConfig",
            negativeAckRedeliveryBackoff = "negativeAckRedeliveryBackoff",
            ackTimeoutRedeliveryBackoff = "ackTimeoutRedeliveryBackoff",
            subscriptionType = SubscriptionType.Shared,
            deadLetterPolicy = "deadLetterPolicy"
    )


    @Transactional(rollbackFor = Exception.class)
    public void processBatch(List<Message<ThumbEvent>> messages) {
        log.info("ThumbConsumer processBatch: {}", messages.size());

//        for(Message<ThumbEvent> message : messages) {
//            log.info("message.getMessageId = {}", message.getMessageId());
//        }
//        if(true) {
//            throw new RuntimeException("测试异常");
//        }

        Map<Long, Long> countMap = new ConcurrentHashMap<>();
        List<Thumb> thumbs = new ArrayList<>();
  
        // 并行处理消息  
        LambdaQueryWrapper<Thumb> wrapper = new LambdaQueryWrapper<>();
        AtomicReference<Boolean> needRemove = new AtomicReference<>(false);
  
        // 提取事件并过滤无效消息  
        List<ThumbEvent> events = messages.stream()  
                .map(Message::getValue)  
                .filter(Objects::nonNull)
                .toList();  

        // 按(userId, blogId)分组，并获取每个分组的最新事件  
        Map<Pair<Long, Long>, ThumbEvent> latestEvents = events.stream()
                .collect(Collectors.groupingBy(
                        e -> Pair.of(e.getUserId(), e.getBlogId()),  
                        Collectors.collectingAndThen(  
                                Collectors.toList(),  
                                list -> {  
                                    // 按时间升序排序，取最后一个作为最新事件  
                                    list.sort(Comparator.comparing(ThumbEvent::getEventTime));
                                    if (list.size() % 2 == 0) {  
                                        return null;  
                                    }  
                                    return list.getLast();
                                }  
                        )  
                ));  
  
        latestEvents.forEach((userBlogPair, event) -> {  
            if (event == null) {  
                return;  
            }  
            ThumbEvent.EventType finalAction = event.getType();  
  
            if (finalAction == ThumbEvent.EventType.INCR) {  
                countMap.merge(event.getBlogId(), 1L, Long::sum);  
                Thumb thumb = new Thumb();  
                thumb.setBlogId(event.getBlogId());  
                thumb.setUserId(event.getUserId());  
                thumbs.add(thumb);  
            } else {  
                needRemove.set(true);  
                wrapper.or().eq(Thumb::getUserId, event.getUserId()).eq(Thumb::getBlogId, event.getBlogId());  
                countMap.merge(event.getBlogId(), -1L, Long::sum);  
            }  
        });  
  
        // 批量更新数据库  
        if (needRemove.get()) {  
            thumbService.remove(wrapper);  
        }  
        batchUpdateBlogs(countMap);  
        batchInsertThumbs(thumbs);  
    }  
  
    public void batchUpdateBlogs(Map<Long, Long> countMap) {  
        if (!countMap.isEmpty()) {  
            blogMapper.batchUpdateThumbCount(countMap);  
        }  
    }  
  
    public void batchInsertThumbs(List<Thumb> thumbs) {  
        if (!thumbs.isEmpty()) {  
            // 分批次插入  
            thumbService.saveBatch(thumbs, 500);  
        }  
    }
}
