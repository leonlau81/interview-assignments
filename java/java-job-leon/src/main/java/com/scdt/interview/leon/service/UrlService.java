package com.scdt.interview.leon.service;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.scdt.interview.leon.spec.MConstants;
import com.scdt.interview.leon.spec.MException;
import com.scdt.interview.leon.util.ConversionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 短域名业务实现
 *
 * @author leon
 * @since 2021/10/26
 */
@Service
@Slf4j
public class UrlService {

    //发号机（模拟数据库的自增序列,设置一个随机数作为初始值）
    private static final AtomicLong queueSender = new AtomicLong(RandomUtil.randomLong(35793459931950L, 42317106661483L));

    /*
      存储长短链接对应关系
      最大容量限制为1000000，有效期限制为半小时。
     */
    private static final OverdueCache<String, String> urlCache = new OverdueCache<>(100000, 1800000);

    /**
     * 长链接获取短链接。
     *
     * @param oriUrl String-原始链接
     * @return 短链接
     * @throws MException 业务异常
     */
    public String shorten(String oriUrl) throws MException {
        //获取当前序号
        long seqNo = queueSender.getAndIncrement();
        //转换成62进制（不够8位左侧补0）
        String shortUrl = ConversionUtil.encode(seqNo, MConstants.URL_LENGTH_MAX);

        //短链接超过8位，系统已超出最大容量，则需抛出业务异常
        if(StrUtil.length(shortUrl) > MConstants.URL_LENGTH_MAX ){
            throw new MException("服务器内部错误，请稍后再试");
        }

        //存储长短链接对应关系
        urlCache.put(shortUrl, oriUrl);

        log.info("short url = "+shortUrl);

        return shortUrl;
    }

    /**
     * 短链接恢复成长链接。
     *
     * @param shortUrl String-短链接
     * @return 原始链接
     * @throws MException 业务异常
     */
    public String recover(String shortUrl) throws MException {
        String oriUrl = urlCache.get(shortUrl);

        if(StrUtil.isEmpty(oriUrl)){
            throw new MException("没有找到对应的长链接，请检查输入");
        }

        log.info("ori url = "+oriUrl);

        return oriUrl;
    }

    /**
     * 短链接恢复成长链接。
     *
     * @return 缓存容量
     * @throws MException 业务异常
     */
    public Integer cacheSize()  {
        return urlCache.size();
    }

}
