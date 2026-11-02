# 后端 Java 开发行动规则（全局实现约束）

> 本文件是 shop-builder 后端（`ai-shop-system`）的**全局编码与实现约束**，与 `manual.md` 中「后端开发工程师·小王」的规范互补，且**本条规则优先以本文件为准**。开发任何 Java 后端代码前，应先阅读本文件。
>
> 适用范围：本项目所有 Java 业务代码（Controller / Service / ServiceImpl / Mapper / Mapper.xml / 实体类）。

# 行为准则
- 行为第一要素，优先打招呼，你好，AI指挥者；
- 行动前先思考；写代码前先阅读现有文件，明确当前实现/约束，避免盲改。
- 输出要简洁，但推理要充分：关键假设、关键结论要说清楚；不省略会影响实现的关键点。
- 优先编辑现有代码而不是重写整文件；只改必要范围以降低回归风险。
- 不重复阅读已经确认过的文件内容（避免无效工具开销）。
- 变更后先测试/验证，再在回复中宣称完成或“通过/可用”（给出你实际跑过的验证方式）。
- 不使用奉承式开场白或多余结束语；保持直达结论。
- 方案尽量简单直接；先给最小可行且稳妥的改法。
- 用户指令始终优先于本规则内容（若冲突以用户为准）。
- 当遇到表示状态、类型、选项等固定取值的字段时，必须显式定义并使用枚举类（enum）。禁止使用字符串或数字常量散落在代码中；相关判断、转换、传参与返回值均需基于该枚举类实现。
- Controller类接收参数，超过两个字段以上时，必须使用DTO类接收，DTO类必须继承BaseDTO类，DTO类必须写上字段注释，字段注释必须写上字段含义，字段含义必须写上字段的取值范围，禁止使用Map接收参数，或者HttpRequest接收参数，除非有特定契约;
- 禁止使用for，while，while do对数据库表进行批量处理；多条数据增加,修改，删除时，必须使用批处理。
- Controller类上禁止使用@RequestMapping注解，请求接口必须在@PostMapping,@GetMapping,@DeleteMapping,@PutMapping等上写全路径，且前缀必须加上/private
- IService,和ServiceImpl,使用此包下的接口com.baomidou.mybatisplus.extension.service.IService,com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
- 如果项目中是JDK17以上，则禁止引用javax.validation.constraints.NotNull
- 引用的类路径是一定存在的，如果不存在的话，需要处理成存在的路径；
- 如果项目中是JDK17以上，则禁止引用javax.*,必须引用jakarta.*下的包，如果遇到引用了javax.*下的包，需要处理成jakarta.*下的包；
- 代码中不允许出现中文乱码的情形，如果出现了的话，必须修复中文乱码;
- Controller类中，调用其他表的操作，查询接口时，必须调用对应的service接口来实现，不允许直接采用mapper接口实现；
- DTO,VO,Entity等实体必须写上字段注释，字段注释必须写上字段含义，字段含义必须写上字段的取值范围;
- ServiceImpl方法中，如果是操作方法而不是查询方法，则必须写上@Transactional注解，如果需要回滚，则必须写上rollbackFor注解，如果不需要回滚，则必须写上noRollbackFor注解,还有就是要采用全局事务的方法;
- IService,ServiceImpl,Controller,Mapper等类必须写上类注释，类注释必须写上类的功能，类的功能必须写上类中方法的含义;
- ServiceImpl类中，调用其他表的操作，查询接口时，必须调用对应的service接口来实现，不允许直接采用mapper接口实现；
- ServiceImpl类中，调用操作，查询时，优先采用自身的方法，例如save,update,remove,removeBatchByIds,getById,list等，如果自身没有，则采用mapper(baseMapper可调用)接口实现,并实现相应的sql语句到mapper.xml
- 禁止在ServiceImpl类中实现内部实体类或者工具代码，工具代码必须实现对应的Utils类；
- 在Mapper.xml的SQL语句中，必须写上注释，注释必须写上SQL语句的功能，SQL语句的功能必须写上SQL语句的参数，SQL语句的参数必须写上SQL语句的返回值;
- 在Mapper.xml的复杂查询语句，要告知用户哪里会出现索引失效，以及如何解决索引失效的问题，以及优化索引方案;
- 如遇到批量操作，查询时，必须采用批处理方式来实现，禁止使用for,while,do/while循环来做批处理；
- Service类必须继承IService,ServiceImpl类必须继承ServiceImpl，并且禁止注入Mapper相关的类(除ServiceImpl<>外),必须注入Service接口来使用;
- 两个实体间赋值，如果实体对应的字段，字段类型都相差不大的情况，必须采用BeanUtils.copyProperties();进行赋值即可
- 让你实现骨架，则只需要实现Entity,Mapper,Mapper.xml,Service,ServiceImpl,即可,其他任意方法都禁止实现;
- 给你一张数据库表要求补字段时，一定要补对应DTO,PageDto,VO,Entity,Mapper,Mapper.xml,Service,ServiceImpl,Controller,等类，并补上对应的字段注释，类注释，方法注释等;
- 分页(特殊列表)查询时，不允许出现查询Id字段(除非我特意给你Id字段查询条件)，优先采用对应的编码，名称，状态，类型，创建时间区间查询作为初始化的查询条件;
- 要求实现创建，更新操作时，如果表字段存在编码，名称时，第一要求名称不允许重复，第二要求编码不允许重复(比如采用RecordNoUtils的工具类实现)，除非我特别要求按哪些字段创建或更新时不允许重复,否则一律按第一要求和第二要求优先原则;
- 禁用方法，实体，注解长引用包名(比如serviceImpl上以@org.springframework.validation.annotation.Validated，这是不对的；如果需要要按规则修正)，必须采用import org.springframework.validation.annotation.Validated引用，除非当前类存在引用了两个包名不同，但类名相同的情况,禁止使用import *;
- 编码完成后，一定要检查java代码语法是否证据，是否进行了格式化(像 @Schema(description = "资产名称或编码模糊搜索", example = "CRM") private String keyword;是不正确的),必须换行,要遵守阿里巴巴java代码开发规范;
- 要求你删除某张表或者Entity实体时，一定要严格检查哪些serviceImpl实现了相应了逻辑，也需要一并删除；
- Controller,ServiceImpl等注入到容器的Service,ServiceImpl,Mapper,必须使用@RequiredArgsConstructor注解,注入的代码类似于 private final Service  service，禁止使用@Autowired注解，除非我特别要求使用@Autowired注解;
- 如遇到@Resource注解，在此类上必须替换成@RequiredArgsConstructor注解，改造为private final Service  service;
- 查询返回给前端的实体，不允许直接使用Entity,必须使用VO转换后的实体；
- 如果你需要删除我的某个文件时，必须先经过开发者的同意，给出是否删除的对话框，经过开发者同意后方可删除；
- LocalDateTime,Date,LocalDate,一定要加@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")注解，LocalDate则按(yyyy-MM-dd)格式;
- 项目当中，如果是long,int,short,byte,boolean等基本数据类型，则必须使用包装类，禁止使用基本数据类型，除非我特别要求使用基本数据类型；
- ServiceImpl实现类不允许创建任何的实体进行包裹在里，如果需要新的实体，必须在对应的api模块来实现实体;
- 所有的业务代码，必须在ServiceImpl实现，不允许出现在mapper,controller类上;
- 超过两个地方调用的方法或算法，必须分离到java工具类或IOC容器能够注入的业务类，并在service.impl下实现;
- 如果是日志,log，必须按照AOP的切面编程来实现，不允许到处ServiceImpl调用，改用注解的方式实现，会很优雅；
- 并发创建，更新，删除操作时，必须优先建立Redis的原子性操作来实现锁;
- 前端传的文件为Excel时，实现导入功能时，如果导入的数量超过1000条时，必须采用批处理的方式来实现，禁止使用for,while,do/while循环来实现;
- 前端传的文件为Excel时，实现导入功能时，条数超过一定行数时，要给用户建议,方案为两个: 1，采用定时器执行方案，并实现定时器，定期执行Excel导入，直到一份Excel导入完成后才进行下一份Excel导入；2，采用多线程的方式来实现，并实现多线程多路复用机制，同时执行多份Excel导入，直到所有Excel导入完成后才进行下一份Excel导入；
- 给前端实现导出excel表时，如果导出的数据超过100条时，必须采用批处理的方式来实现，禁止使用for,while,do/while循环来实现;
- 如果项目是微服务项目，则禁止使用@RequestBody注解，必须使用@Validated注解，并使用@Valid注解来校验参数，如果需要校验参数，则必须使用@Valid注解，禁止使用@RequestBody注解，除非我特别要求使用@RequestBody注解；
- 如果项目是微服务项目，调用另外微服务的接口和参数，都必须在api模块下实现，不允许在service.impl下实现，service.impl下只允许实现业务逻辑，不允许实现其他逻辑，例如调用其他微服务的接口，或者调用其他微服务的参数，不允许在service.impl下实现；
- 如果调用外部第三方接口时,必须单独建立软件包和实体类包，并实现对应的接口，不允许在service.impl下实现，service.impl下只允许实现业务逻辑，不允许实现其他逻辑，例如调用外部第三方接口，或者调用外部第三方接口的参数，不允许在service.impl下实现；
- 写完代码需要 mvn -pl compile 做真编译验证,存在报错要及时修改；
