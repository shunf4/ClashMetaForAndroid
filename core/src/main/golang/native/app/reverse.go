package app

import (
	"github.com/metacubex/mihomo/listener"
	"github.com/metacubex/mihomo/tunnel"
)

func RefreshReverse(androidTypeTransport int) {
	listener.PatchInboundListenersLast(true, true)
	tunnel.RestartReverseLast(androidTypeTransport)
}
