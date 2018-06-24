# micro-service-template

## 简述

一个微服务模板的架子，大概包括如下几个部分

- **api**：定义该微服务的接口信息，该接口定义遵从OpenAPI格式定义，并通过swagger生成相关代码框架
- **client**：按字面意思，是该微服务提供给用户的客户端组件，但是其并不是简单的组件。该组件被server所依赖，他们共用client中定义的`common工具包、exception包、constant常量包等`微服务级别公用信息
- **server**：微服务服务端主进程，其中实现了微服务的全部功能
    - **impl**：微服务接口实现的入口，这里完成api定义、rest请求分发等功能
    - **dispatch**：rest请求分发的直接依赖包，其中每个子包应该定义一类（一个controller）功能
        - BaseHandler：基于模板方法实现的单个rest-api父类，其中完成的功能有
            1. 对接口实现统一try-catch-finally，防止忘记异常处理
            2. 如果rest-api实现使用的`ServiceResponse`作为泛型实参，那么对这种返回做异常处理（正常的使用默认值就行）
            3. 提供多个扩展实现方法：写成功操作日志、写失败操作日志、自定义异常返回、前置/后置操作
        - Returnable：定义各种异常返回码，并通过`ServiceResponse`返回出去
        - ServiceResponse：通用异常返回类，其中包含`retCode和retInfo`信息，分别表示返回报文的返回码和返回信息，它和HTTP状态有所不同
            1. HTTP状态码对于错误信息比较单一，有时候需要更加详尽的错误码来表示不同的错误
            2. HTTP状态码与返回码是一对多的关系，同样的400错误可能对应更加丰富的客户端异常信息
    - **proxy**：该微服务所依赖的第三系统的代理实现
        - Proxy：一个IOC/DI容器，基于Guice/Spring实现，推荐使用guice，并使用单例模式
        - ProxyException：使用Returnable中的Proxy异常返回码定义，该包及其子包下的所有的异常均需使用或实现该异常