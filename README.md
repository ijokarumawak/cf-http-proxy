# cf-http-proxy
A simple HTTP forward proxy using little-proxy that can be run in Cloud Foundry.

## Prerequisite

This document assumes that you have a Cloud Foundry endpoint.
If not, [PCF Dev tutorial](https://pivotal.io/platform/pcf-tutorials/getting-started-with-pivotal-cloud-foundry-dev/introduction) is recommended to setup one in your local environment including the `cf` command.

## How to build and deploy to Cloud Foundry

```bash
$ mvn clean package
$ cf push
```

Then look at the logs to see if it has successfully started:

```
$ cf logs http-proxy

2017-03-15T11:11:19.42+0900 [APP/PROC/WEB/0]OUT A proxy server started successfully on port 8080
2017-03-15T11:11:21.12+0900 [CELL/0]     OUT Container became healthy
```

Although the proxy server is started, it can not be used from outside yet, because it listens on localhost:8080.
This is for application portability, as written in this Cloud Foundry Documentation [HTTP vs. TCP Routes](https://docs.cloudfoundry.org/devguide/deploy-apps/routes-domains.html):

> Applications should listen to the localhost port defined by the $PORT environment variable, which is 8080 on Diego.

We need to add tcp port mapping to this application endpoint.

### Create a TCP shared domain

In PCF Dev env, there's already a tcp shared domain like below:

```bash
$ cf domains
Getting domains in org pcfdev-org as user...
name                  status   type
local.pcfdev.io       shared
tcp.local.pcfdev.io   shared   tcp
```

If you don't have one, you can create it by referring this Cloud Foundry Documentation: [Create a Shared Domain](https://docs.cloudfoundry.org/devguide/deploy-apps/routes-domains.html#http-vs-tcp-shared-domains).

To create a shared domain, you need to login with admin user:

```bash
$ cf login -a api.local.pcfdev.io --skip-ssl-validation
Email>     admin
Password>  admin
```

### Create a TCP route to HTTP Proxy app

To make the proxy app accessible from outside of CF, let's create a TCP route:

```bash
$ cf create-route pcfdev-space tcp.local.pcfdev.io --random-port

# Confirm the randomly assigned port number. It doesn't have target app at this point.
$ cf routes
Getting routes for org pcfdev-org / space pcfdev-space as user ...

space          host           domain                port    path   type   apps           service
pcfdev-space   http-proxy     local.pcfdev.io                             http-proxy
pcfdev-space                  tcp.local.pcfdev.io   61054          tcp
```

Then add a mapping to the route toward HTTP Proxy app:

```bash
$ cf map-route http-proxy tcp.local.pcfdev.io --port 61054

Creating route tcp.local.pcfdev.io:61054 for org pcfdev-org / space pcfdev-space as user...
OK
Route tcp.local.pcfdev.io:61054 already exists
Adding route tcp.local.pcfdev.io:61054 to app http-proxy in org pcfdev-org / space pcfdev-space as user...
OK
```

## Use Proxy from cURL command

As an example, the proxy can be used from a cURL command like this:

```bash
$ curl -I -x tcp.local.pcfdev.io:61054 http://www.example.com/
```

## Use Proxy from NiFi RemoteProcessGroup

Imagine a remote NiFi instance is running where your NiFi can not directly access, but Cloud Foundry can.
Those two NiFi instances can transfer data via Site-to-Site protocol, by deploying this cf-http-proxy and specify it from a client NiFi RemoteProcessGroup.

A RemoteProcessGroup in client NiFi data flow:

![](https://github.com/ijokarumawak/cf-http-proxy/blob/master/docs/images/nifi-flow-example.png?raw=true)

Configuration of the client RemoteProcessGroup:

![](https://github.com/ijokarumawak/cf-http-proxy/blob/master/docs/images/nifi-rpg-conf.png?raw=true)
