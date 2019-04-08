# LRUCache

A data structure in Java 8 for Least Recently Used (LRU) cache service, supported the following operations: get and put.

get(key) - Get the value (will always be positive) of the key if the key exists in the cache, otherwise it will return 404.
put(key, value) - Set or insert the value if the key is not already present. When the cache reached its capacity, it will invalidate the least recently used item before inserting a new item, returning the invalidated item.

These operations are to be externalized as a service exposed by a REST API.

Example:
LRUCache cache = new LRUCache(capacity=2);

Example:
curl XPUT http://cache.service/api/v1/put/1 -d "value=400"
200
{
}

curl XPUT http://cache.service/api/v1/put/2 -d "value=800"
200
{
}

curl XGET http://cache.service/api/v1/get/1
200
{
  key: 1,
  value: 400
}

curl XPUT http://cache.service/api/v1/put/3 -d "value=1200"  //evicts key=2
200
{
  key: 2,
  value: 800
}

curl XGET http://cache.service/api/v1/get/2
404

curl XPUT http://cache.service/api/v1/put/4 -d "value=1600"  //evicts key=1
200
{
  key: 1,
  value: 400
}

curl XGET http://cache.service/api/v1/get/1
404

curl XGET http://cache.service/api/v1/get/3
200
{
  key: 3,
  value: 1200
}

curl XGET http://cache.service/api/v1/get/4
200
{
  key: 4,
  value: 1600
}

Frameworks used: Vertx (for http server and client), JUnit 4.
