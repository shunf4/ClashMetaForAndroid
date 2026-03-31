package tunnel

import (
	"context"
	"sync"
	"time"

	"github.com/metacubex/mihomo/adapter/outboundgroup"
	"github.com/metacubex/mihomo/common/utils"
	"github.com/metacubex/mihomo/constant/provider"
	"github.com/metacubex/mihomo/log"
	"github.com/metacubex/mihomo/tunnel"
)

func HealthCheck(name string) {
	p := tunnel.Proxies()[name]

	if p == nil {
		log.Warnln("Request health check for `%s`: not found", name)

		return
	}

	overwriteHealthCheckUrl := ""
	// todo: expectedStatus overwrite group?
	var expectedStatus utils.IntRanges[uint16] = nil
	// healthCheckOneUrl := C.DefaultTestURL
	// todo: C.DefaultTestURL www.gstatic.com doesn't work for my some proxies. change it entirely later?
	healthCheckOneUrl := "https://www.googleapis.com/auth/documents"
	if overwriteHealthCheckUrl != "" {
		healthCheckOneUrl = overwriteHealthCheckUrl
	}
	allowOneProxyTest := true

	g, ok := p.Adapter().(outboundgroup.ProxyGroup)
	if !ok {
		if allowOneProxyTest {
			// One, Single
			ctx, cancel := context.WithTimeout(context.Background(), time.Millisecond*6000)
			defer cancel()

			delay, err := p.URLTest(ctx, healthCheckOneUrl, expectedStatus)

			// core/src/foss/golang/clash/adapter/provider/healthcheck.go execute ignores err also

			log.Infoln("Health Checked from Android bridge, proxy: %s, url: %s, alive: %t, delay: %d ms / (stored?) %d ms, err: %+#v", p.Name(), healthCheckOneUrl, p.AliveForTestUrl(healthCheckOneUrl), delay, p.LastDelayForTestUrl(healthCheckOneUrl), err)

			return
		} else {

			log.Warnln("Request health check for `%s`: invalid type %s", name, p.Type().String())

			return
		}
	}

	wg := &sync.WaitGroup{}

	for _, pr := range g.Providers() {
		wg.Add(1)

		go func(provider provider.ProxyProvider) {
			provider.HealthCheck()

			wg.Done()
		}(pr)
	}

	wg.Wait()
}

func HealthCheckAll() {
	for _, g := range QueryProxyGroupNames(false) {
		go func(group string) {
			HealthCheck(group)
		}(g)
	}
}
