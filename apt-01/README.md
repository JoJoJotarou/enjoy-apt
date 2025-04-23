## 最佳实现

APT 开发引入

```xml

<dependency>
    <groupId>com.google.auto.service</groupId>
    <artifactId>auto-service</artifactId>
    <version>1.1.1</version>
</dependency>
```

在实现的Processor类上添加注解

```java

@AutoService(Processor.class)
public class MyProcessor extends AbstractProcessor {
    // ...
}
```

这样即可在项目编译时，自动生成 `META-INF/services/javax.annotation.processing.Processor` 文件，内容为实现类的全限定名。

## 遇到的问题

若不使用 `@AutoService` 则需要在 `META-INF/services/javax.annotation.processing.Processor` 文件中手动添加实现类的全限定名。
若该文件与Processor类在同一个项目中则会引发异常：
`服务配置文件不正确, 或构造处理程序对象javax.annotation.processing.Processor: Provider com.example.MyProcessor not found`

那怎么办呢？

需呀将 `META-INF/services/javax.annotation.processing.Processor` 移动他其他项目中，且这个其他项目依然包含Processor实现类的项目

为什么会发生这种情况呢？

根据[这篇文章](https://blog.csdn.net/wyanyi/article/details/125686058)的解释：

编译的时候，javac会去找所有jar包及项目（模块）里resource/META-INF/services/javax.annotation.processing.Processor这个文件中配置的类信息，
记住是类信息，它会通过classloader去加载这个类，此时项目（模块）中的文件因为是在编译期，尚未生成class文件，自然也就找不到对应的类