package com.juesecc.websocket.ioNetty;/**
 * @author wangcc
 * @create
 */

import org.springframework.beans.factory.wiring.BeanWiringInfo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @ClassName IndexController
 * @Author wangcc
 * @Date 20:13 2020/7/25
 **/
@Controller
@RequestMapping("/ws")
public class IndexController {


    @GetMapping("/index")
    public ModelAndView  index(){
        ModelAndView mav=new ModelAndView("socket");
        Random random = new Random();
        Map<Integer, String> map = new HashMap<>();
        map.put(0,"MTY=");
        mav.addObject("uid",  map.get(0));
//                map.put(1,"Mg==");
//        mav.addObject("uid",  map.get(random.nextInt(2)));
//        random.nextInt(10)
        return mav;
    }

}

