package com.github.bdqfork.client.ops;

import com.github.bdqfork.core.operation.ValueOperation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;

public class ValueOperationTest {
    JRedisClient client;
    ValueOperation operation;

    @Before
    public void setUp() throws Exception {
        client = new JRedisClient("127.0.0.1", 7000, 0);
        client.connect();
        operation = client.OpsForValue();
    }

    @After
    public void tearDown() throws Exception {
        client.close();
    }

    @Test
    public void set() {
        User user = new User();
        user.username = "test1";
        user.password = "pass";
        operation.set("test1", user);
        get("test1");
    }

    @Test
    public void setex() {
        User user = new User();
        user.username = "test2";
        user.password = "pass";
        operation.setex("test2", user, 20);
        get("test2");
    }

    @Test
    public void setpx() {
        User user = new User();
        user.username = "test3";
        user.password = "pass";
        operation.setpx("test3", user, 20000);
        get("test3");
    }

    @Test
    public void setnx() {
        User user = new User();
        user.username = "test4";
        user.password = "pass";
        operation.setnx("test4", user);
        operation.setnx("test5", user);
        get("test3");
        get("test4");
    }

    @Test
    public void setxx() {
        User user = new User();
        user.username = "test5";
        user.password = "pass";
        operation.setxx("test5", user);
        operation.setxx("test6", user);
        get("test5");
        get("test6");
    }

    @Test
    public void getTest() {
        get("test4");
        get("test5");
        client.OpsForKey().del("test4");
        get("test4");
    }

    public void get(String key) {
        User user = operation.get(key);
        System.out.println(user);
    }

    static class User implements Serializable {
        /**
         *
         */
        private static final long serialVersionUID = 1L;
        String username;
        String password;

        @Override
        public String toString() {
            return "User{" + "username='" + username + '\'' + ", password='" + password + '\'' + '}';
        }
    }

}