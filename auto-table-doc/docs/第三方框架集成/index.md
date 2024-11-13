---
title: 第三方框架集成
description:
aside: false
---

目前在Mybatis生态中，存在着许多主流的ORM框架，例如：MybatisPlus、MybatisFlex等，他们分别在表名、列名、多数据源、主键、字段排除等多个方面与`AutoTable`
有着重复的概念，对于框架使用者而言，需要额外编写多套注解，且标注同样的内容，不免有些违背了我们简化工作的初衷，因此，AutoTable提供了一套接口化抽象，让框架开发者可以轻松实现对AutoTable的拓展，这样就可以在主流框架中统一一套注解实现自动维护表结构的功能。

::: warning 强烈建议

如果你使用的ORM框架有集成了AutoTable的拓展，那么强烈建议你直接使用框架的拓展包，而不是自行引入AutoTable，拓展框架，能帮你省去大量的兼容适配工作。

:::

<!-- @include: @/common/ORM框架支持表格.md-->

***可参照左侧的菜单。***
