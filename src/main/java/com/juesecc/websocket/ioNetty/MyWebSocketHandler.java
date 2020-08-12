package com.juesecc.websocket.ioNetty;/**
 * @author wangcc
 * @create
 */

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.Transient;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName MyWebSocketHandler
 * @Author wangcc
 * @Date 20:10 2020/7/25
 **/
public class MyWebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private static Logger logger = LoggerFactory.getLogger(MyWebSocketHandler.class);

    //用户id=>channel示例
    //可以通过用户的唯一标识保存用户的channel
    //这样就可以发送给指定的用户
    public static ConcurrentHashMap<String, Channel> channelMap = new ConcurrentHashMap<>();


    /**
     *每当服务端断开客户端连接时,客户端的channel从ChannelGroup中移除,并通知列表中其他客户端channel
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        //获取连接的channel
        /*Channel incomming = ctx.channel();
        for(Channel channel:channelGroup){
            channel.writeAndFlush("[SERVER]-"+incomming.remoteAddress()+"离开\n");
        }*/
        //从服务端的channelGroup中移除当前离开的客户端
        MyChannelHandlerPool.channelGroup.remove(ctx.channel());

        //从服务端的channelMap中移除当前离开的客户端
        Collection<Channel> col = channelMap.values();
        while(true == col.contains(ctx.channel())) {
            col.remove(ctx.channel());
            logger.info("netty客户端连接删除成功!");
        }

    }
    /**
     * 每当服务端收到新的客户端连接时,客户端的channel存入ChannelGroup列表中,并通知列表中其他客户端channel
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        //获取连接的channel
        Channel incomming = ctx.channel();
        //通知所有已经连接到服务器的客户端，有一个新的通道加入
        /*for(Channel channel:channelGroup){
            channel.writeAndFlush("[SERVER]-"+incomming.remoteAddress()+"加入\n");
        }*/
        MyChannelHandlerPool.channelGroup.add(ctx.channel());
    }
    /**
     * 服务端监听到客户端活动
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("与客户端建立连接，通道开启！");

        //添加到channelGroup通道组
        MyChannelHandlerPool.channelGroup.add(ctx.channel());
    }

    /**
     * 服务端监听到客户端不活动
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("ID: "+getKey(MyChannelHandlerPool.channelIdMap,ctx.channel().id())+"与客户端断开连接，通道关闭！");
    }

    /**
     * 每当从服务端读到客户端写入信息时,将信息转发给其他客户端的Channel.
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        logger.info("netty客户端收到服务器数据: {}" , msg.text());
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String msgText = msg.text();
        JSONObject jsonObject = JSONObject.parseObject(msgText);
        Map<String, Object> map = jsonObject;
        String longId = ctx.channel().id().asLongText();
        if (map.containsKey("login")) {
            login(map, ctx.channel().id());
        } else {
            sendToSomeone(map);
        }

    }

    /**
     * 当服务端的IO 抛出异常时被调用
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //super.exceptionCaught(ctx, cause);
            Channel incoming = ctx.channel();
            System.out.println("SimpleChatClient:" + incoming.remoteAddress()+"异常");
            //异常出现就关闭连接
            cause.printStackTrace();
            ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        //用于触发用户事件，包含触发读空闲、写空闲、读写空闲
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.ALL_IDLE) {
                Channel channel = ctx.channel();
                //关闭无用channel，以防资源浪费
                channel.close();
            }
        }
    }

    private void sendAllMessage(String message){
        //收到信息后，群发给所有channel
        MyChannelHandlerPool.channelGroup.writeAndFlush( new TextWebSocketFrame(message));
    }


    private void sendToSomeone(Map<String, Object> map) {
        ChannelId fromId = MyChannelHandlerPool.channelIdMap.get(map.get("fromId"));
        ChannelId toId = MyChannelHandlerPool.channelIdMap.get(map.get("toId"));
        if(MyChannelHandlerPool.channelGroup.find(fromId)!=null&&MyChannelHandlerPool.channelGroup.find(toId)!=null){
            MyChannelHandlerPool.channelGroup.find(fromId).writeAndFlush(new TextWebSocketFrame(map.toString()));
            MyChannelHandlerPool.channelGroup.find(toId).writeAndFlush(new TextWebSocketFrame(map.toString()));
        }else{
            System.out.println("发送的对象不存在");
            MyChannelHandlerPool.channelGroup.find(fromId).writeAndFlush(new TextWebSocketFrame("发送的对象不存在,请重新输入"));
        }

    }

    private void login(Map<String, Object> map, ChannelId channelId) {
        String uid = map.get("login").toString();
//        redisOperator.set(6, uid, channelId);
        MyChannelHandlerPool.channelIdMap.put(uid, channelId);
    }

    private static String getKey(Map<String, ChannelId> map, Object value) {
        String key = "";
        for (Map.Entry<String, ChannelId> entry : map.entrySet()) {
            if (value.equals(entry.getValue())) {
                key = entry.getKey();
                continue;
            }
        }
        return key;
    }
}
