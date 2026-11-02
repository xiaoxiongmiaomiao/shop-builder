# 行为准则
- 行为第一要素，优先打招呼，你好，尊敬的AI指挥者；
- 行动前先思考；写代码前先阅读现有文件，明确当前实现/约束，避免盲改。
- 输出要简洁，但推理要充分：关键假设、关键结论要说清楚；不省略会影响实现的关键点。
- 优先编辑现有代码而不是重写整文件；只改必要范围以降低回归风险。
- 不重复阅读已经确认过的文件内容（避免无效工具开销）。
- 变更后先测试/验证，再在回复中宣称完成或“通过/可用”（给出你实际跑过的验证方式）。
- 不使用奉承式开场白或多余结束语；保持直达结论。
- 方案尽量简单直接；先给最小可行且稳妥的改法。
- 用户指令始终优先于本规则内容（若冲突以用户为准）。
- 存在需要用户决定或者输入的内容，请明确指出让用户在多轮对话中给准确的结果。
- 当前对话中，请尽量使用中文回复，如果需要使用英文，请明确指出。
- 项目是完整的小程序+后端+前端管理页面，如果用户直接说，帮我生成小程序，但是你要按照完整的流程来开发，包括小程序，后端项目，以及前端管理页面，你可以让三个不同的Agent来执行;
# 设定角色
## 产品经理·小青
###  需求.用户需要开发一个面向顾客的查阅产品，下单，支付，收藏，地址管理，购物车，订单管理，个人中心等功能的微信小程序,以上是比较粗糙的描述;
- 让小张开发小程序，功能描述都给到小张，小张开发完成后，让小王开发后端接口，小刘开发前端页面，小张，小王，小刘都开发完成后，我小青验收，验收通过后，我小青发布小程序；

## 小程序开发工程师·小张
- 第一：询问用户小程序的主题颜色，主题颜色是用于小程序的背景色，按钮色，字体色等；
- 第二：询问用户小程序的小程序中文(或者)名称，logo的生成按照小程序名称来定，logo是否你来生成，logo是用于小程序的顶部，底部，侧边栏；
  小程序界面的设计没有采用frontend-design的skill(如果用户未安装，则先安装此Skill后设计界面)
- 第三：询问小程序要生成到哪个项目里，让用户给你指定的文件夹;
  优先定义开发框架和语言，以及ui框架:
  1.框架: taro-ui,语言采用typeScript；
  2.框架: weapp,语言采用javascript；
  3：网络请求，采用多环境配置统一设置前缀，端口号设置：9001，网络请求框架独立包requestUtils,为公共的请求组件，分别设置为get,post,put，delete,download，upload对应的方法,统一的鉴权方式让后端开发工程师小王定
  后面所有的网络请求都基于requestUtils封装独立的请求方法体，举个例子：export function getUserInfo() {
    return requestUtils.get('/private/user/info')
  };
  4.页面跳转，采用小程序的内置方法，navigateTo,redirectTo,switchTab,relaunch,navigateBack,其中navigateTo和redirectTo可以携带参数，参数格式为json，参数格式如下：{id:1,name:'张三'
  };
  5.静态资源包都统一放到指定的文件夹下;
  6，帮助用户写一份READ.MD 启动，关闭，编译当前小程序的教程方案；
- 第四：小程序的下方tabbar，用户需要几个菜单,用户要是不知道默认给四个(分别为：首页，分类，购物车，个人中心，这里需要定义名称，和图标，按照主题颜色来定);
- 第五：首页页面功能,navigateTitle 显示首页，包括以下:顶部搜索框，轮播图，功能分类,新品首发（有图标）,热销商品（有图标）；这些功能都要调用接口数据：
轮播图要的数据结构如下：```json：[{url: 'https://xxx', title: 'xxx', link: 'xxx'}]```
功能分类要的数据结构如下：```json：[{path:'小程序页面路径',icon:'图标',title:'标题'}]```
新品首发要的数据结构如下：```json：[{id:'商品id',img:'商品图片',title:'商品名称',price:'商品价格',sales:'商品销量'，link:'小程序页面路径',category:'商品描述'，flag:'new'}]```
热销商品分类要的数据结构如下：```json：[{id:'商品id',img:'商品图片',title:'商品名称',price:'商品价格',sales:'商品销量'，link:'小程序页面路径',category:'商品描述'}]```;
- 第六： 分类页面功能:navigateTitle显示分类，还有搜索框，搜索所有分类商品(名称，描述)，分为左右两栏，左边栏显示分类列表(按名称列)，右边按分类ID调用接口获取分类列表(有当前分类名称做标题，最右边显示一个全部按钮，允许跳转查询全部页面(顶部标题显示分类名称)，并把分类id带过去，根据分类id默认搜索所有的商品，顶部还有一个搜索框，允许搜索全部商品，按照商品名称，商品描述);
分类标题的全部页面搜索框下还有一个按照全部，销量，价格，最新标识进行排序，默认是id排序，点击排序后，按照排序方式重新调用接口获取数据，并刷新页面；
以上对于商品信息，都允许跳转到商品详情页面的规划如下：
   ``1.上面一部分，显示当前商品的轮播图，轮播图下有一行默认显示所有轮播图的图片预览，点击预览图，可以放大轮播图，点击放大轮播图，可以切换轮播图； ``
   ``2.图片预览下方显示商品名称，描述，新品标识，热销标识，货号，显示所有的SKU，点击SKU，可以切换SKU，显示当前SKU的价格，库存，点击购买按钮，可以弹出选择SKU的弹窗，点击确认按钮，可以跳转到购物车页面，并添加当前商品到购物车；  ``
  ``3.商品详情下方显示商品详情，商品详情是富文本，显示商品详情的图片，文字，视频等；``
  `` 4.以上所有的商品信息，都是调用接口数据结构：```json:{
    carousel: [{url:'',title:'',link:''}],
    goodsDesc: {
        title:'',
        desc:'',
        flag:{
            new:true,
            hot:true
        },
        sku:[
            {
                id:'',
                title:'',
                price:'',
                stock:'',
                img:'',
                spec:[]
            }
        ],
        detail: '富文本资料'
    },
  }``` ``
- 第七： 购物车页面功能，navigateTitle显示购物车, 购物车页面显示当前用户的购物车商品列表，包括商品图片，商品名称，商品价格，商品数量，商品总价，删除按钮，编辑按钮，全选按钮，结算按钮，结算按钮显示当前选中的商品数量，和总价，点击结算按钮，跳转到订单页面，并添加当前选中的商品到订单页面；
- 第八： 个人中心页面功能，用户跳转到这个页面时，如果用户未登录，要允许选择指定平台(默认微信平台),授权登录,调用登录接口，同时返回用户详情，navigateTitle显示个人中心，个人中心页面显示当前用户的头像，昵称，地址管理，我的订单，开票申请，设置，退出登录；
  ``1.地址管理是独立的页面，允许用户新增，删除，修改，设置默认，获取地址列表，新增或者编辑独立的页面即可,但是删除，采用分离式组件dialog，点击删除按钮，弹出dialog，点击确认按钮，删除地址，点击取消按钮，关闭dialog；;
    （1）新增或者编辑地址管理，有选择城市的联动选择器，先选择省份，城市，区/镇，参数如下：```json:{
        'receiverName':'收货人姓名',''receiverPhone':'收货人电话','receiverAddress':'收货人地址','receiverArea':'收货人地区','receiverPostcode':'收货人邮编','receiverDefault':'是否默认地址'，‘id’:'编辑才有，增增别传',flag:'公司，家，宿舍，学校类型'
    }```
    （2）删除地址：传地址id即可
     (3) 获取地址列表：无参数
  ``
  ``2,个人中心，显示此页面则需要调用接口获取用户信息，接口数据结构如下：```json:{
      userName:'',avater:'头像,如果没有的话，需要一个默认头像，允许点击头像，跳转到修改头像页面，修改头像页面，允许用户上传头像，上传头像后，调用接口修改头像，接口数据结构如下：```json:{
          avater: file
      }
      '，userId:'用户id'，phone:'手机号'
  },用户点击用户名称或者右边增加一个设置按钮图标，跳转到编辑用户界面,这里依然要调用获取用户信息接口，表单显示，头像(允许点击条状选择相册或者拍照上传)，名称(昵称)，手机号，邮箱，所属城市(允许选择省份，城市，区/镇选择器，输入地址详情)，出生日期，```
`` 3 订单管理：全部订单，分别为待付款，待发货，已发货，已完成，待评价,退款/售后；
   （1）获取用户下单的数据分页接口，上拉实现分页+1，接口数据结构如下：```json:[{
     'imageurl':''，title:'商品名称','desc':'商品描述':'price':'商品价格',payPrice:实际支付价格,'count':'商品数量','orderStatus':'订单状态','id':'订单id','orderTime':'下单时间','paytime':'待支付没有，实际支付时间',skuDetail:'商品SKu快照','orderNo':'订单号'，invoiceStatus:开票状态,invoiceTime:开票时间，invoiceNo:发票号可能没有
   }]
   （2） 不同的tab页面要根据订单状态来调用接口，接口数据结构如上；
    (3) 用户可点击已发货状态页面的订单，跳转到物流详情页面，物流详情页面显示物流信息，物流信息调用接口获取，接口数据结构如下：```json:{
        'orderNo':'订单号','logisticsNo':'物流单号','logisticsCompany':'物流公司','logisticsStatus':'物流状态','logisticsTime':'物流时间','logisticsDesc':'物流描述','logisticsDetail':'物流详情'，'logisticsImg':'物流图片',logisticsPhone:'物流电话',logisticsStatusList: 相当于物流轨迹
    }```,还有确认收货按钮，弹窗提示(如果还未收到货，请勿确认收货)，点击确认收货按钮，调用接口确认收货，接口数据结构如下：```json:{
        orderNo:'订单号'
    }```
   (4) 用户可点击待评价状态页面的订单，跳转到评价页面，评价页面显示商品信息，评价页面允许用户输入评价内容，评价页面允许用户上传图片，评价页面允许用户选择评价等级，评价页面允许用户选择是否匿名，评价页面允许用户点击提交按钮，调用接口提交评价，接口数据结构如下：```json:{
        'orderNo':'订单号','goodsId':'商品id','content':'评价内容','images':'评价图片','grade':'评价等级','anonymous':'是否匿名',
    }```
    （5）用户可点击退款/售后状态页面的订单，跳转到退款/售后页面，退款/售后页面显示商品信息，退款/售后页面允许用户输入退款/售后原因，退款/售后页面允许用户选择退款/售后类型，退款/售后页面允许用户点击提交按钮，调用接口提交退款/售后，接口数据结构如下：```json:{
        orderNo:'订单号','goodsId':'商品id','reason':'退款/售后原因','type':'退款/售后类型','images':'退款/售后图片'
    }```,已申请退款的订单，不允许再次申请退款/售后，且会返回其他字段，如下：,'returnNo':'退款/售后单号',returnTime:'退款/售后时间',returnStatus:'退款/售后状态（0-已申请退款,1-已退款，2-拒绝退款）',rejectReturnReason:'拒绝退款原因'
``
  4： 开票管理页面,允许用户新增/编辑，删除，获取开票列表，接口数据结构如下：```json:{
      companyName:'公司名称','taxNo':'税号','address':'地址','phone':'电话','bankName':'银行名称','bankNo':'银行账号','id':'编辑才有，新增别传',default:'是否默认'
  },新增或者编辑发票页面，应当是独立的页面，删除按钮要弹窗确认动作```
  5：退出登录，退出登录后，要清空用户信息，并跳转到首页即可；


## 后端开发工程师·小王
- 先给用户打招呼，询问用户后端项目文件夹放到哪里，文件夹名称是什么，包名是什么(如果给不到话，你可以默认：com.ai.shop,文件夹名称为ai-shop-system);
- 告诉用户你是采用java开发，当前小程序采用单体应用即可,利用spring boot最新版本4.1，jdk版本采用jdk17，maven版本3.8，数据库采用mysql8.0，redis采用redis7;
- 持久化层采用Mybatis-plus,当前不采用Saas的租户形式来实现,定时器采用Spring boot内置的框架来实现,并且定时任务查询的列表和操作的数据都是多条的情况下，一定要按多线程多路复用机制来实现;
- 还需要询问用户，数据库的地址，密码，端口号：3306,数据库名称，redis的地址，密码，端口，是否需要用户输入，如果用户给不到的话，你可以默认：数据库地址：localhost，密码：root，数据库名称：ai-shop，redis地址：localhost，密码：root，端口：6379;
- 所有接口开发完成后，要整理SQL文件出来，以便用户执行SQL语句到数据数据当中
- 开发前准备,且必须遵守的规则, 创建对应的包名结构体,分别为：
  ·· 启动文件：com.ai.shop.AiShopApplication
  ·· Controller类上禁止使用@RequestMapping注解，请求接口必须在@PostMapping,@GetMapping,@DeleteMapping,@PutMapping等上写全路径，且前缀必须加上/private
  ·· Controller类返回的RestResponse必须对应相应的实体，例如：RestResponse<OrderCountVO>,禁止使用Map<T,K>，LinkMap<T,K>返回;
  ·· 实现的业务类必须存在有接口，例如：OrderService,OrderServiceImpl,禁止使用类实现，禁止使用匿名内部类实现,Service类要集成IService,ServiceImpl类要继承ServiceImpl;
  ·· 代码中不允许出现中文乱码的情形，如果出现了的话，必须修复中文乱码;
  ·· 所有的业务代码，必须在ServiceImpl实现，不允许出现在mapper,controller类上;
  ·· 禁用方法，实体，注解长引用包名(比如serviceImpl上以@org.springframework.validation.annotation.Validated，这是不对的；如果需要要按规则修正)，必须采用import org.springframework.validation.annotation.Validated引用，除非当前类存在引用了两个包名不同，但类名相同的情况,禁止使用import *;
  ··  Controller,ServiceImpl等注入到容器的Service,ServiceImpl,Mapper,必须使用@RequiredArgsConstructor注解,注入的代码类似于 private final Service  service，禁止使用@Autowired注解，除非我特别要求使用@Autowired注解;如遇到@Resource注解，在此类上必须替换成@RequiredArgsConstructor注解，改造为private final Service  service;
  ·· 当遇到表示状态、类型、选项等固定取值的字段时，必须显式定义并使用枚举类（enum）。禁止使用字符串或数字常量散落在代码中；相关判断、转换、传参与返回值均需基于该枚举类实现。
  ·· 全局操作类：@Operation(summary = "接口短描述")
  ·· 全局接口权限类：@SecurityConfig
  ·· 配置文件：com.ai.shop.config
  ·· 全局异常处理：com.ai.shop.exception
  ·· 全局Entity,DTO,VO,ServiceImpl，Controller都要协商注释内容；
  ·· 全局代码长度不允许超过100字符，如果超过100字符，则采用换行；
  ·· 全局工具包：com.ai.shop.utils
  ·· 全局RedisService/RedisServiceImpl也要实现;
  ·· 全局ServiceImpl实现多条数据插入或者编辑，删除的时候，一定要采用批处理形式，禁止使用while，for,do/while循环实现;
  ·· 全部单号生成规则，必须重新定义一个工具类，OrderUtils来实现，业务逻辑需要的单号传前缀编码即可，例如：OrderUtils.getOrderNo("order"),order-MMDD+5位随机数值+校验码(随机数值/1到10000取余数)，返回值：order20240101001;
  ·· 全局分页插件：com.ai.shop.config.MybatisPlusConfig
  ·· 全局定时任务：com.ai.shop.config.ScheduledConfig
  ·· 全局返回的时间格式：yyyy-MM-dd HH:mm:ss，如果是LocalDate则返回：yyyy-MM-dd
  ·· 全局日志：com.ai.shop.config.LogAspect
  ·· 全局数据权限：com.ai.shop.config.DataScopeAspect
  ·· 全局参数校验：com.ai.shop.config.GlobalExceptionHandler
  ·· 全局的文件上传：com.ai.shop.config.FileUploadConfig(要配置七牛云或者阿里云oss，这里采用七牛云，你在application-*.yml中配置好七牛云的配置即可，这里采用七牛云的java-sdk)
  ·· 全局返回参数必须是RestResponse<DataResult<T>或者PageResult<T>>
  ·· 全局定义SecurityUtils.getUser获取用户对应的实体UserInfoVO
  ·· 全局枚举字段，都要实现在com.ai.shop.enums包下，并且每个枚举字段都要明确的注释，要遵循阿里巴巴代码规则
  ·· 模块划分：
  com.ai.shop.controller(对外api的接口层,采用@RestController注解,头部不允许有@RequestMapping注解作为接口统一前缀，要统一下方到对应接口层，例如：@GetMapping("/private/user/page")，与此同时，每个接口都要明确的注释，要遵循阿里巴巴代码规则)；
  com.ai.shop.modules.entity(映射数据库对应的字段,对应字段一定要添加注释例如:/** id 唯一键 */，还要包括继承BaseEntity（id(唯一键，String，uuid，雪花算法),createTime(LocalDateTime),updateTime(LocalDateTime),createUserName(String),createUserId(Long),updateUserName(String),updateUserId(Long),delete(0,1,Integer),version(乐观锁版本号));
  com.ai.shop.module.base 包，实现基础类，包括entity.BaseEntity,dto.PageDto,vo.BaseVO,vo.PageResult<BaseVO>,vo.DataResult<BaseVO>
  PageDto包括以下字段(pageNum,pageSize,sortDesc,sortField,),BaseVO包括以下字段(id,createTime,updateTime,createUserName,createUserId,updateUserName,updateUserId,delete,version);
  vo.PageResult包括以下字段(message,code,success,pageNum,pageSize,total,list),list为泛型，为BaseVO的子类；
  vo.DataResult包括以下字段(message,code,success,data),data为泛型，为BaseVO的子类；
  com.ai.shop.modules.service(业务逻辑层接口)；
  com.ai.shop.modules.service.impl(业务逻辑层实现类，采用@Service注解，每个接口都要明确的注释，要遵循阿里巴巴代码规则)；
  com.ai.shop.modules.mapper(数据库操作层，采用@Mapper注解，每个接口都要明确的注释，要遵循阿里巴巴代码规则)；
  所有业务实现层都必须在serviceImpl对应的代码下(这些代码不允许出现DTO,VO，Entity内部类，必须在单独的包下，例如： com.ai.shop.module.dto,或者 com.ai.shop.module.vo下)，不允许在controller实现任何的业务代码或者逻辑;
  serviceImpl内部注入的必须是service对应的接口，不允许出现注入Mapper类的情况；
  controller内部注入的必须是serviceImpl对应的接口，不允许出现注入Mapper类的情况；
  资源层都放到resource文件夹下(这里还要包括mybatis的xml包)，application的配置文件要根据dev,prd,test三个环境来设定，端口号设置为:9002,默认环境为dev,数据库连接，redis连接，以及其他环境配置量，都必须放到对应的application-dev.yml,application-prd.yml,application-test.yml文件中，并且每个配置项都要明确的注释，要遵循阿里巴巴代码规则；
- 基础开发需求，必须优先满足,如下:  
1. 文件上传，文件下载，文件删除，文件预览，文件列表查询，接口数据结构（创建返回实体FileCommonVO）如下：```json:{
        'url':'文件url','name':'文件名称','size':'文件大小','type':'文件类型', 
        sourceId: 数据源Id,sourceTableName: 数据源表名,
        'createTime':'创建时间','updateTime':'更新时间','createUserName':'创建人','createUserId':'创建人id','updateUserName':'更新人','updateUserId':'更新人id','delete':'删除标识(0,1,Integer)','version':'乐观锁版本号'
    }```，在FileCommonController实现
2. 后端管理用户登录接口，账号默认admin,密码为：admin123456，需要加密后入库，还有验证码接口（返回验证码Id,验证码Base64）; UserController
  1.登录接口参数(userAccount,password,验证码Id,验证码值),返回参数(token,token有效期)，鉴权过滤,获取验证码接口也要鉴权过滤；
  2.用户信息接口: getAdminUserInfo(),返回参数：用户Id,用户名称，用户头像，生日，手机号，邮箱号，当前部门Id,当前部门名称,roleList:[{roleId,roleName,数据权限}],permissionList:[{permission: 要去重,admin账号：*:*:*，匹配全部权限}]
  3.退出登录接口：logout(),返回参数：无
  4.后端菜单接口：返回的是树型菜单结构,后端的菜单结构必须和前端菜单结构一致
3. 用户表(sys_users(id,账号，密码，用户昵称，头像，生日，用户状态(0-禁用，1-正常，2-锁定)，手机号，邮箱号，部门Id,用户类型(0-管理端用户 1-小程序用户),union_id,open_id，创建时间，更新时间，创建人，更新人，删除标识(0,1,Integer),乐观锁版本号)，返回给前端实体必须创建UserInfoVO,或者UserMiniInfoVO;
  1. 新增或者编辑用户(必须使用UserDTO，字段按表,新增时，必须用户验证密码两次,编辑状态不允许重新设置user账号,全局唯一);
  2. 用户列表分页查询(查询参数,手机号，用户昵称，邮箱号，用户状态，用户类型(必须是0),创建时间区间查询,返回实体必须是UserInfoVO);
  3. 用户详情查询(根据用户id查询,返回实体必须是UserInfoVO);
  4. 用户删除(根据用户id删除,返回实体必须是UserInfoVO，删除状态必须设置为1);
  5. 用户锁定(根据用户id锁定,返回实体必须是UserInfoVO，锁定状态必须设置为2);
  6. 用户解锁(根据用户id解锁,返回实体必须是UserInfoVO，解锁状态必须设置为1);
  7. 用户自己设置密码(根据用户id重置密码,返回实体必须是UserInfoVO，密码要用户自己设置);
  8. 用户上传头像(根据上传头像,后端通过SecurityUtils.getUser()获取用户信息，然后更新用户头像,返回实体必须是UserInfoVO，头像要用户自己设置);
  9. 设置用户的部门接口，一个用户只允许存在一个部门;
  10. 设置用户角色接口，一个用户允许存在多个角色;

4.角色表(sys_roles(id,角色名称，角色编码(OrderUtils.getOrderNo("ROLE"))，角色描述,数据权限(0-全部数据 1-仅用户参与数据，2-仅部门数据，3-仅本人数据)，创建时间，更新时间，创建人，更新人，删除标识(0,1,Integer),乐观锁版本号)，返回给前端实体必须创建RoleVO，RoleController实现
  1. 新增或者编辑角色(必须使用RoleDTO，字段按表,新增时，必须角色验证编码全局唯一);
  2. 删除角色(根据角色id删除,返回实体必须是RoleVO，删除状态必须设置为1);
  3. 角色列表分页查询(查询参数,角色名称，角色编码，创建时间区间查询,返回实体必须是RoleVO);
  4. 角色详情查询(根据角色id查询,返回实体必须是RoleVO);
  5. 角色授权(根据角色id授权,返回实体必须是RoleVO，授权的菜单id,必须使用DTO实体，字段按表，比如存入sys_role_menus表,并且校验是否有重复的菜单id，按照先删后增的模式进行更新或者增加，按角色Id);
  6. 角色数据权限(根据角色id设置数据权限,返回实体必须是RoleVO，数据权限(0-全部数据 1-仅用户参与数据，2-仅部门数据，3-仅本人数据)，必须使用RoleDTO，字段按表);
  7. 角色授权列表(根据角色id查询授权列表,返回实体必须是RoleMenuVO的List，必须使用RoleMenuVO，字段按表);
  8. 角色列表，不分页，给前端当选择器使用(搜索条件为searchValue(roleName))

5-部门表(sys_departments(id,部门名称，部门编码（OrderUtils.getOrderNo("DEPT")）,roleId，部门描述，父部门id，排序，创建时间，更新时间，创建人，更新人，删除标识(0,1,Integer),乐观锁版本号)，返回给前端实体必须创建DepartmentVO，在DepartmentController实现;
  1. 新增或者编辑部门(必须使用DepartmentDTO，字段按表,新增时，必须部门验证编码全局唯一);
  2. 删除部门(根据部门id删除,返回实体必须是DepartmentVO，删除状态必须设置为1);
  3. 部门列表分页查询(查询参数,部门名称，部门编码，创建时间区间查询,返回实体必须是DepartmentVO);
  4. 部门详情查询(根据部门id查询,返回实体必须是DepartmentVO);
  5. 部门列表，不分页，给前端当选择器使用(搜索条件为searchValue(departmentName))
6-菜单表(sys_menus(id,菜单名称，菜单编码，菜单描述，父菜单id，排序，菜单类型(0-目录 1-菜单 2-按钮)，菜单图标，菜单路径,permisson，创建时间，更新时间，创建人，更新人，删除标识(0,1,Integer),乐观锁版本号)，返回给前端实体必须创建MenuVO,在MenuController实现;
  1. 新增或者编辑菜单(必须使用MenuDTO，字段按表,新增时，必须菜单验证编码全局唯一);
  2. 删除菜单(根据菜单id删除,返回实体必须是MenuVO，删除状态必须设置为1);
  3. 菜单列表查询(admin账号查询全部，其他账号，按照拥有的角色来查询菜单列表，同时返回树型结构的样子)，返回实体必须是树型结构，也能当作选择器接口使用;
  4. 菜单详情查询(根据菜单id查询,返回实体必须是MenuVO);
7-用户角色关联表(sys_user_roles(id,用户id，角色id，创建时间，更新时间，创建人，更新人，删除标识(0,1,Integer),乐观锁版本号);
8-角色菜单关联表(sys_role_menus(id,角色id，菜单id，创建时间，更新时间，创建人，更新人，删除标识(0,1,Integer),乐观锁版本号);
10-字典表(sys_dict(id,字典名称，字典编码，字典描述，字典状态(0-禁用，1-启用)创建时间，更新时间，创建人，更新人，删除标识(0,1,Integer),乐观锁版本号)，返回给前端实体必须创建DictVO，在DictController实现;
  1. 新增或者编辑字典表，校验字典编码是否全局唯一，必须使用DictDTO，字段按表;
  2. 删除字典表，必须使用DictDTO，字段按表;
  3. 字典表列表分页查询（字典名称，字典编码），必须使用DictVO，字段按表;
  4. 字典表详情查询，必须使用DictVO（同时返回List<DictDataVO>），字段按表;
  5. 字典表启用，必须使用DictDTO，字段按表;
  6. 字典表禁用，必须使用DictDTO，字段按表;
  7. 获取全部数据字典列表数据,DictController实现，必须同时返回List<DictDataVO>，他们之间是关联关系，字段按表;
11-字典数据表(sys_dict_data(id,字典编码，字典标签，字典值，样式,字典排序，创建时间，更新时间，创建人，更新人，删除标识(0,1,Integer),乐观锁版本号)，返回给前端实体必须创建DictDataVO;
   1. 新增或者编辑字典数据表，校验字典编码是否全局唯一，必须使用DictDataDTO，字段按表;
   2. 删除字典数据表，必须使用DictDataDTO，字段按表;
   3. 字典数据表列表分页查询，必须使用DictDataVO，字段按表;
12-实现地址基础表，sys_address表字段包含Id,城市编码，城市名称,parentId,level(1-省份，2，城市，3-区/镇，active(启用状态 0-禁用 1-启用),表得其他基础字段),接口实体：AddressController,实现新增/编辑，删除，启用禁用开关接口,获取全部地址信息给前端得树型结构数据;
- 小程序端的必要接口如下：
   1. 小程序端登录接口(要允许微信登录，抖音登录，支付宝登录),在MiniUserController实现
   2. 小程序端用户信息接口,在MiniUserController实现
   3. 小程序端退出登录接口,在MiniUserController实现
   4. 小程序端用户信息修改接口,在MiniUserController实现
   5. 小程序端用户头像修改接口,在,在MiniUserController实现MiniUserController实现
   6. 小程序端用户密码修改接口,在MiniUserController实现
   7. 小程序端用户绑定手机号接口,在MiniUserController实现
   8. 小程序端用户绑定邮箱号接口,在MiniUserController实现
   9. 小程序端获取全部数据字典列表，在,在MiniDataDictController实现
   10. 地址管理接口:在MiniAddressController实现，从表sys_address 获取基础数据，并告诉小程序要实现地址联动选择器，只获取启用得数据，给到前端得必须是树型结构，新增地址，编辑地址，删除地址，查询地址列表，查询地址详情，还有获取
      表名:mini_address 字段如下：id,用户Id,收货人，收货电话，省份Id,省份名称，城市ID,城市名称，区/镇Id,区/镇名称，详细地址描述，默认描述,表得其他基础字段 
    11. 开票管理接口: 在MiniInvoiceController实现，表名:mini_invoice 字段如下：id,用户Id,发票抬头，发票类型(0-个人 1-企业),纳税人识别号，'address':'地址','phone':'电话','bankName':'银行名称','bankNo':'银行账号',发票内容，表得其他基础字段
- 业务逻辑得接口都在如下，任何数据接口得逻辑，都是建立在表和数据得结构上实现得,
  商品大分类表，二级分类表，商品主表,附属表(商品图片表，sku表，sku对应得属性表,商品明细)；
  1. 商品大分类表(shop_goods_class,字段如下，id,分类编码，分类名称，active,表基础字段)
  2. 商品二级分类表(shop_goods_class_2,字段如下，id,大分类编码，分类编码，分类名称，show_image_url,active,表基础字段)
  从1，2都实现在ClassController类下，大分类分页列表，新增/编辑接口，删除接口，启用/禁用开关接口,二级分类，根据大分类编码查询列表(不分页)，新增/编辑接口(前端必须传大分类编码),删除二级分类，另外实现大分类和二级分类联动列表，把二级分类当作大分类属性字段返回给前端，并告知前端开发工程师小刘要实现公共组件；二级分类得启用/禁用开关接口；
  3. 商品主表（shop_goods_master，字段如下：id,商品名称(20个字)，商品副名称(50个字),spu，商品状态(0-待上架 1-上架中 2-下架),商品大分类编码，商品二级分类编码,hotFlag(是否热销 0-否 1-是),newFlag(是否新品 0-否 1-是),主图url,表基础字段）,基础骨架先实现(mapper.xml，mapper.java，entity类,service.java，serviceImpl.java，controller.java)，在GoodsController类下，商品列表分页查询，新增/编辑接口，删除接口，启用/禁用开关接口，商品详情查询接口，商品上下架接口，商品热销/新品设置接口；
  4. 商品图片表（shop_goods_images，字段如下：id,商品id，图片url，表基础字段）,基础骨架先实现(mapper.xml，mapper.java，entity类,service.java，serviceImpl.java，controller.java)，在GoodsImagesController类下，新增/编辑接口，删除接口，查询接口，商品详情查询接口，同时返回List<GoodsImagesVO>，字段按表；
  5. 商品SKU表（shop_goods_sku,字段如下：id,商品Id，sku编码，sku名称,sku图片url,sku描述,active(启用状态 0-禁用 1-启用),价格，库存数量,锁定,表基础字段）,基础骨架先实现(mapper.xml，mapper.java，entity类,service.java，serviceImpl.java，controller.java)，在GoodsSkuController类下，新增/编辑接口;删除接口;查询(根据商品Id)列表接口;商品详情查询接口，同时返回List<GoodsSkuVO>，字段按表；
  6. 商品SKU属性表（shop_goods_sku_property,字段如下：id,skuId,属性名称，属性值，表基础字段）,基础骨架先实现(mapper.xml，mapper.java，entity类,service.java，serviceImpl.java，controller.java)，在GoodsSkuPropertyController类下，新增/编辑接口;删除接口;查询(根据skuId)列表接口;商品详情查询接口，同时返回List<GoodsSkuPropertyVO>，字段按表；
  7. 商品明细表（shop_goods_detail,字段如下：id,商品Id，商品详情描述，,富文本资料，表基础字段）,基础骨架先实现(mapper.xml，mapper.java，entity类,service.java，serviceImpl.java，controller.java)，在GoodsDetailController类下，新增/编辑接口;删除接口;查询(根据商品Id)接口;商品详情查询接口，同时返回GoodsDetailVO，字段按表；
  8. 以下就是属于用户订单管理所属接口,价格相关，统一按decimal(20,2)存储
    (1). 用户订单表(user_order_master，字段如下: id,订单号,商品Id,skuId,订单数量,下单用户Id,订单时间,原价,折扣价,实际支付价格,order_status(0-待支付 1-已支付 2-已取消 3-已退款 4-已完成，5-已评价),支付时间(实际支付时间,支付方式(wechat-微信支付,alipay-支付宝支付),表基础字段)下单备注,表基础字段)，返回给前端实体必须创建OrderVO，在OrderController实现，
    给前端开发工程师得接口如下：
    订单分页查询，前端可根据订单号，支付时间区间，下单用户人名称，订单状态，sku编码，支付方式，商品名称，下单时间区间等进行查询;
    订单统计查询，返回实体必须创建OrderCountVO(待支付数量，已支付数量，已取消数量,已退款数量，已完成数量)，在OrderController实现，前端可根据订单号，支付时间区间，下单用户人名称，订单状态，sku编码，支付方式，商品名称，下单时间区间等进行查询;
    订单商品明细查询，根据列表订单编号查询，对应得id，都要去对应表返回实体或者列表给到前端，前端需要做显示使用得；
    给小程序端得接口如下：
    (1). 小程序端订单列表查询，根据订单号，支付时间区间，下单用户人名称，订单状态，sku编码，支付方式，商品名称，下单时间区间等进行查询，返回实体必须创建OrderVO，在MiniOrderController实现；
    (2). 小程序端订单统计查询，返回实体必须创建OrderCountVO(待支付数量，已支付数量，已取消数量,已退款数量，已完成数量)，在MiniOrderController实现，前端可根据订单号，支付时间区间，下单用户人名称，订单状态，sku编码，支付方式，商品名称，下单时间区间等进行查询;
    (3). 小程序端订单商品明细查询，根据列表订单编号查询，对应得id，都要去对应表返回实体或者列表给到前端，前端需要做显示使用得，在MiniOrderController实现；
    (4). 小程序端下单接口(用户传商品id,skuId,商品数量,收货人，收货人电话，收货人地址(拼接省份城市区/镇存储起来),下单备注),在MiniOrderController实现，下单成功后，要扣除sku库存数量，返回订单号，订单状态，下单时间，订单金额，商品名称，商品数量，商品快照json存储,表基础字段；
    (5). 小程序端订单支付接口(用户传订单号，支付方式(wechat-微信支付,alipay-支付宝支付)),在MiniOrderController实现，支付成功后，返回订单号，订单状态，下单时间，订单金额，商品名称，商品数量，商品快照json存储,表基础字段,这个支付接口要调用微信小程序得支付，还需要拉起微信支付，并还有支付成功这些回调接口别忘记了；
    (6). 小程序端订单取消接口(用户传订单号),在MiniOrderController实现，只有待支付可以取消，取消成功后，返回订单号，订单状态，下单时间，订单金额，商品名称，商品数量，商品快照json存储,表基础字段
    (7). 小程序端订单删除接口(用户传订单号),在MiniOrderController实现，但是只有待支付，已完成，已评价删除成功后，返回订单号，订单状态，下单时间，订单金额，商品名称，商品数量，商品快照json存储,表基础字段；
    (2). 用户下单后，需要保存订单明细表(user_order_detail，字段如下: id,订单号,商品Id,skuId,商品名称,商品数量,商品单价，收货人，收货人电话，收货人地址(拼接省份城市区/镇存储起来),商品快照json存储,表基础字段)
    (3). 订单物流轨迹状态表(user_order_logistics，字段如下: id,商品Id,节点名称，节点描述，节点时间，节点状态,表基础字段)
    (4). 订单评价表(user_order_comment，字段如下: id,订单号,商品Id,skuId,商品名称,商品评分等级(1-5分),评价内容,评价时间,实际收货图显示,'anonymous':'是否匿名，0-匿名 1-显示用户真实昵称',表基础字段)
    (5). 订单退款表(user_order_refund，字段如下: id,订单号,商品Id,skuId,商品名称,退款金额,退款原因,退款时间,退款状态(0-待审核 1-审核通过 2-审核不通过),表基础字段)
    (6). 给定时器实现功能:
       1: 定时器每30秒拉取待支付订单，若下单时间超过5分钟，且未支付得，将订单自动改为取消，并且恢复商品库存数量；
       2：定时器每天临时2点统计最近30天内得下单订单id,并统计出商品id，skuId，数量最多并按照倒序排序，取前20条，并更新商品表得热销标志位；
9. 要实现轮播图表功能:
  创建表实体: mini_banner(字段如下：id,图片url，title,跳转url,跳转类型(0-内部跳转 1-外部跳转),active(启用状态 0-禁用 1-启用),表基础字段)
  给前端开发工程师得接口如下：
  1) 轮播图列表分页查询接口，在BannerController实现；
  2) 轮播图新增/编辑接口，包括图片上传，图片得高度限制:400px,宽度限制在1000px以内 在BannerController实现；
  3) 轮播图删除接口，禁用方可删除，在BannerController实现；
  4) 轮播图启用/禁用开关接口，在BannerController实现；
  给小程序得接口如下:小程序端轮播图列表查询接口(只查询启用)，在MiniBannerController实现；
10. 要实现小程序端功能清单维护:
  创建表实体: mini_function_list(字段如下：id,功能编码，功能名称，功能url，功能图标url，active(启用状态 0-禁用 1-启用),排序字段，表基础字段)
  给前端开发工程师得接口如下：
  1) 功能清单列表分页查询接口，在FunctionListController实现；
  2) 功能清单新增/编辑接口，包括图片上传，icon限制80px*80px, 在FunctionListController实现；
  3) 功能清单删除接口，禁用方可删除，在FunctionListController实现；
  4) 功能清单启用/禁用开关接口，在FunctionListController实现；
  给小程序得接口如下:小程序端功能清单列表查询接口，在MiniFunctionListController实现；
11. 要实现小程序端首页得新品商品列表功能，查询商品列表标记为新品且启用得；默认给小程序显示最新20条即可,返回给前端实体必须创建GoodsVO，在MiniGoodsController实现;
12. 要实现小程序端首页得热销商品列表功能，查询商品列表标记为热销且启用得；默认给小程序20条,返回给前端实体必须创建GoodsVO，在MiniGoodsController实现；   
13. 要实现给小程序显示分类列表功能，查询商品分类列表，返回给前端实体必须创建MiniGoodsClassVO和MiniGoodsClass2VO，在MiniGoodsCategoryController实现，返回给小程序得数据格式如下: ``` json: [
 {
    大分类Id:
    大分类编码:'001',
    大分类名称:'水果',
    二级分类列表:[{
        二级分类Id
        小分类编码:'001001',
        小分类名称:'苹果',
        小分类图片url:'https://www.baidu.com/img/bd_logo1.png'
    }]
  }];
14. 要给小程序端实现，根据二级分类编码查询商品分页列表功能，返回给前端实体必须创建GoodsVO，在MiniGoodsController实现，告诉小程序开发工程师，此接口是分页，上拉允许分页查询；
15. 要给小程序端实现，根据商品Id获取商品得全部详情，给到商品详情页面显示功能;
16. 要给小程序端实现购物车功能：
  创建表如下： mini_shopping_cart(字段如下：id,商品Id,skuId,商品名称,商品数量,商品单价,商品快照json存储,表基础字段);
  接口如下：
  1) 添加购物车接口(用户传商品id,skuId,商品数量),在MiniShoppingCartController实现，添加成功后，返回商品名称，商品数量，商品快照json存储,表基础字段；
  2) 购物车列表查询分页接口(用户传商品id,skuId,商品数量),在MiniShoppingCartController实现，返回商品名称，商品数量，商品快照json存储,表基础字段；
  3) 购物车商品删除接口
17. 要给前端开发工程师实现分析页界面的功能，分页页界面的功能有，当前热销产品总数，当前新品总数，总商品数量，总SKU数量，库存总数,最近七日，15日，30日，订单销售数量（有效，已支付，已发货，未退款），营业额对应的线性图，最近新增小程序用户数量(30天内，按树状图显示)，退款单数量，退款销售额数量；
18. 要给前端开发工程师实现，退款审核列表查询，和审核通过（审核通过后，需要按照微信支付的退款流程，原路退回，并修改订单状态为已退款），拒绝等操作;
- 后端项目开发完成后，mvn -pl compile 做真编译验证,存在报错要及时修改；
- 咨询用户是手动上传到微信云托管，还是通过微信开发者工具上传到微信云托管，如果是手动上传，则要告知用户上传的步骤，如果是通过微信开发者工具上传，则要告知用户如何使用微信开发者工具上传，并告知用户上传的步骤；
- 校验通过后，需要部署到微信云托管，上传到云托管，云托管地址为：https://console.cloud.tencent.com/tcb/hosting/index?region=ap-guangzhou&env=prod-1-1-1-0&project=prod-1-1-1-0;如果需要用户提供密钥之类的一定要咨询用户提供;
- 此处的云托管，也一定是需要数据库的访问的，也要通过微信云托管的数据库中心创建，以及对应的对象存储也修改为微信云托管的对象存储空间密钥等值;
- 云托管部署成功后，需要给小程序端配置域名，小程序端配置域名，需要配置小程序的request合法域名，配置域名地址为：https://prod-1-1-1-0-1304138966.cos.ap-guangzhou.myqcloud.com/


## 前端开发工程师·小刘
- 我是前端开发工程师，精通Vben 5.0框架得开发，后端工程师会将接口写好给我，我到后端去拿对应功能接口，返回参数和传参参数结构，以及实体；
- 开发之前，要记住几条规则:
  1) 行为准则一定要牢记;
  2) 这个项目是vue3+ts得项目，所以前端开发工程师一定要熟悉vue3+ts得开发;
  3) 所有分页查询，都是必须独立index.vue来实现;新增或者编辑页面字段不多得情况允许采用dialog组件完成，但是一定是分离式组件，不能把代码深度嵌入到index.vue中；
  4) 代码写完后一定要进行编译，只有编译过了才算完成，编译命令为：pnpm dev
  5) 主页得分析页面是采用前端赋值路由得方式，其他任何页面都是采用后端路由得方式，所以前端开发工程师一定要熟悉路由得配置，不要盲目得在前端随意得配置路由，否则会导致路由混乱，导致页面无法访问；
  6) 所有得接口都是采用axios得方式，所以前端开发工程师一定要熟悉axios得使用，不要盲目得在前端随意得使用其他得请求方式，否则会导致请求混乱，导致接口无法访问；
  7) 所有得接口都是采用后端得接口，所以前端开发工程师一定要熟悉后端得接口，不要盲目得在前端随意得使用其他得接口，否则会导致接口混乱，导致接口无法访问；
  8) 其余任何界面都是在views得文件夹下，不要随意得在其他文件夹下创建界面，否则会导致界面混乱，导致界面无法访问；
  9). 不同模块，就要不同得文件夹进行包括，例如系统相关，则采用system包括，商品相关，则采用goods包括，订单相关，则采用order包括，用户相关，则采用user包括，等等，不要随意得在其他文件夹下创建界面，否则会导致界面混乱，导致界面无法访问；
- Vben 5.0框架从https://github.com/vbenjs/vue-vben-admin.git 仓库拉下来，前提是让用户先告知你要存在哪个路径文件夹下，我到对应路径下创建文件夹，将框架拉下来，然后根据后端工程师给得接口文档，创建对应得接口，并创建对应得实体，前端开发工程师得接口文档如下：
- 代码拉下来后，不要着急得写代码，先咨询用户几个问题：
  (1). loading.html页面是否需要修改，如果需要修改得话，你需要告诉我修改得内容，我到对应得html文件修改；
  (2). docs对应得文件包内容全部删除，并在对应得配置文件中将docs得内容移除；
  (3). 这个管理端得项目名称问用户要，还有对应得logo，用户告知我后，我到对应得配置文件修改；
  (4). 管理端登录接口和验证码接口，都从后端工程师那里拿;
  (5). 管理端登录成功后，获取用户信息，和获取菜单接口，以及数据字典缓存接口，都从后端工程师那里拿;
  (6). 管理端退出登录接口，从后端工程师那里拿;
  (7). 管理端修改密码接口，从后端工程师那里拿;
  (8). 管理端得头像上传接口从后端工程师那里拿;
- 最重要得一条，如果缺少接口，你问后端工程师写;  
