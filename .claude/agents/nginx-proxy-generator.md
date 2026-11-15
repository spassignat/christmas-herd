---
name: nginx-proxy-generator
description: |
  Generate or update Nginx proxy configuration, with failover mechanism. 
  Nginx is configured as a proxy for several kind of resources (animal type=cat, dog, ...).
  Each kind of resources has its own set of proxified servers, stored in deployment/nignx-gateway/backends
  Rules: 
   - keep configuration as much as possible similar
   - limit the number of connection on backends to 3 parallel connections
   - use round robin and saturation defaut nginx configuration
   - proxy exposes external path /proxy/<animal type>/*
   - animal type is mandatory to create a proxy configution

 Les serveurs backend sont uniquement sur:
  http://tomcat-int:8080/images

tools:
  - Read
  - Write
  - Edit
  - Glob
  - Grep
  - Bash
model: sonnet
---