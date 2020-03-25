package test.imp;

import test.api.Demo;
import test.api.UserDTO;

public class DemoImp implements Demo {
    public UserDTO sayHello(String name) {
        String word = "Hello ! 服务器收到了 ! ";
        UserDTO user = new UserDTO(name,word);
        return user;
    }
}
