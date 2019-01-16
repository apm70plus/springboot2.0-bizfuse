## Spring Data扩展

### Querydsl基本用法
参考官网[Querying JPA](http://www.querydsl.com/static/querydsl/latest/reference/html/ch02.html#jpa_integration)

### Querydsl扩展
Querydsl是一个框架,通过它的流式API构建静态类型的SQL类查询。多个Spring Data模块通过QueryDslPredicateExecutor与Querydsl集成。

- QueryDslPredicateExecutor接口（详细请移步官方文档）
```java
public interface QuerydslPredicateExecutor<T> {

	Optional<T> findOne(Predicate predicate);

	Iterable<T> findAll(Predicate predicate);

	Iterable<T> findAll(Predicate predicate, Sort sort);

	Iterable<T> findAll(Predicate predicate, OrderSpecifier<?>... orders);

	Iterable<T> findAll(OrderSpecifier<?>... orders);

	Page<T> findAll(Predicate predicate, Pageable pageable);

	long count(Predicate predicate);

	boolean exists(Predicate predicate);
}
```

- 仓库集成QueryDsl
```java
interface UserRepository extends CrudRepository<User, Long>, QueryDslPredicateExecutor<User> {
}
```

- 使用Querydsl的Predicate书写类型安全的查询
```java
Predicate predicate = user
    .firstname.equalsIgnoreCase("dave")
    .and(user.lastname.startsWithIgnoreCase("mathews"));
userRepository.findAll(predicate);

```

- Querydsl web 支持
添加一个@QuerydslPredicate到一个方法签名将提供一个就绪的Predicate  
```java
@Controller
class UserController {
  @Autowired UserRepository repository;
  
  @RequestMapping(value = "/", method = RequestMethod.GET)
  Page<User> find(@QuerydslPredicate(root = User.class) Predicate predicate,  ①
              Pageable pageable, 
              @RequestParam MultiValueMap<String, String> parameters) {
        return repository.findAll(predicate, pageable));
  }
}

①为User转换匹配查询字符串参数的Predicate

默认的绑定规则如下:

Object在简单属性上如同eq
Object在集合作为属性如同contains
Collection在简单属性上如同in
```

这些绑定可以通过@QuerydslPredicate的bindings属性定制或者使用Java8default methods给仓库接口添加QuerydslBinderCustomizer  
```java
pubilc Interface UserReposotory extends CurdRepository<User, String>,
    QueryDslPredicateExecutor<User>,  ①
    QuerydslBinderCustomizer<QUser> {  ②  
    
    @Override
    default public void customize(QuerydslBindings bindings, QUser user) {
        bindings.bind(user.username).first((path, value) -> path.contains(value));  ③
        bindings.bind(String.class).first(
            (StringPath path, String value) -> path.containsIgnoreCase(value));  ④
        bindings.excluding(user.password);  ⑤
    }
}
  
① QueryDslPredicateExecutor为Predicate提供特殊的查询方法提供入口
② 在仓库接口定义QuerydslBinderCustomizer将自动注解@QuerydslPredicate(bindings=…)
③ 为username属性定义绑定,绑定到一个简单集合
④ 为String属性定义默认绑定到一个不区分大小写的集合
⑤ 从Predicate移除密码属性

```

### 参考
[Spring Data Extensions](https://docs.spring.io/spring-data/cassandra/docs/current/reference/html/#core.extensions)
