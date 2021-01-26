package xyz.cofe.dist.layout

import org.junit.jupiter.api.Test

import java.util.regex.Pattern

class WildcardTest {
    @Test
    void test01(){
        Pattern ptrn = Wildcard.wildcard("Hello*world")
        println(ptrn)
        println(ptrn.matcher('hello world').matches())
        println(ptrn.matcher('hello12344 world').matches())
        println(ptrn.matcher('hell12344 world').matches())
    }
}
