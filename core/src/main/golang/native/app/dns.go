package app

import (
	"strings"

	"github.com/metacubex/mihomo/dns"
	"github.com/metacubex/mihomo/log"
)

func NotifyDnsChanged(dnsList string) {
	var addr []string
	if len(dnsList) > 0 {
		addr = strings.Split(dnsList, ",")
	}
	log.Infoln("System DNS changed: %v, updating and flushing cache", addr)
	dns.UpdateSystemDNS(addr)
	dns.FlushCacheWithDefaultResolver()
}
