







```bash
$ curl http://localhost:8080/customers
[{"id":1,"firstName":"hantsy@master","lastName":"bai"},{"id":2,"firstName":"Hantsy@DemoApplication","lastName":"Bai"},
$ curl http://localhost:8080/customers -H "X-Tenant-Id:tenant2"
[{"id":5,"firstName":"hantsy@tenant2","lastName":"bai"}]
$ curl http://localhost:8080/customers -H "X-Tenant-Id:tenant1"
[{"id":5,"firstName":"hantsy@tenant1","lastName":"bai"}]
```



