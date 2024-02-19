package app

import (
	"strings"

	"github.com/metacubex/mihomo/dns"
	"github.com/metacubex/mihomo/log"
)

func NotifyDnsChanged(dnsList string) {
	dL := strings.Split(dnsList, ",")

	ns := make([]dns.NameServer, 0, len(dnsList))
	for _, d := range dL {
		ns = append(ns, dns.NameServer{Addr: d})
	}

	log.Infoln("System DNS changed: %v, updating and flushing cache", dL)
	dns.UpdateSystemDNS(dL)
	dns.FlushCacheWithDefaultResolver()
}
