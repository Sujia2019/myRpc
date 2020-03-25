package test.api;

import java.io.Serializable;

public class UserDTO implements Serializable {

    private static final long serialVersionUID = 2L;
    private String name;
    private String word;
     public UserDTO(String name,String word){
         this.name = name;
         this.word = word;
     }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    @Override
    public String toString(){
         return "User{" +
                 "name ='" +name+
                 " , word = "+word+
                 " }";
    }
}
