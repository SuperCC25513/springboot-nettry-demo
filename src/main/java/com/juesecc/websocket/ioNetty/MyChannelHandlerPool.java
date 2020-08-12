package com.juesecc.websocket.ioNetty;/**
 * @author wangcc
 * @create
 */

import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName MyChannelHandlerPool
 * @Author wangcc
 * @Date 20:08 2020/7/25
 **/
public class MyChannelHandlerPool {

    public MyChannelHandlerPool(){}

    public static Map<String, ChannelId> channelIdMap = new HashMap<>();

    public static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

}