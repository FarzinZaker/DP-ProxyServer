package web

class UrlMappings {

    static mappings = {

        "/favicon.ico"(controller: 'proxy', action: 'ignore')
        "/prepareSlave"(controller: 'proxy', action: 'prepareSlave')
        "/createTestActor"(controller: 'proxy', action: 'createTestActor')
        "/configuration/$action?/$id?(.$format)?"(controller: 'configuration')
        "/scenarioConfig/$action?/$id?(.$format)?"(controller: 'scenarioConfig')
        "/adaptationOption/$action?/$id?(.$format)?"(controller: 'adaptationOption')
        "/proxy/data/$id?(.$format)?"(controller: 'proxy', action: 'data')
        "/proxy/report/$id?(.$format)?"(controller: 'proxy', action: 'report')
        "/proxy/reset/$id?(.$format)?"(controller: 'proxy', action: 'reset')
        "/rt/**"(controller: 'proxy', action: 'rt')
        "/**"(controller: 'proxy') {
            action = [POST: "post", GET: "get", PUT: "put", DELETE: "delete"]
        }

        "/"(view: "/index")
        "500"(view: '/error')
        "404"(view: '/notFound')
    }
}
