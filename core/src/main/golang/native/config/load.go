package config

import (
	"io/ioutil"
	// "os"
	P "path"
	"runtime"

	// "runtime/pprof"
	"strings"
	// "time"

	"gopkg.in/yaml.v2"

	"cfa/native/app"

	"github.com/metacubex/mihomo/log"

	"github.com/metacubex/mihomo/config"
	"github.com/metacubex/mihomo/hub/executor"
)

func logDns(cfg *config.RawConfig) {
	bytes, err := yaml.Marshal(&cfg.DNS)
	if err != nil {
		log.Warnln("Marshal dns: %s", err.Error())

		return
	}

	log.Infoln("dns:")

	for _, line := range strings.Split(string(bytes), "\n") {
		log.Infoln("  %s", line)
	}
}

func UnmarshalAndPatch(profilePath string) (*config.RawConfig, error) {
	configPath := P.Join(profilePath, "config.yaml")

	configData, err := ioutil.ReadFile(configPath)
	if err != nil {
		return nil, err
	}

	rawConfig, err := config.UnmarshalRawConfig(configData)
	if err != nil {
		return nil, err
	}

	if err := process(rawConfig, profilePath); err != nil {
		return nil, err
	}

	return rawConfig, nil
}

func Parse(rawConfig *config.RawConfig) (*config.Config, error) {
	cfg, err := config.ParseRawConfig(rawConfig)
	if err != nil {
		return nil, err
	}

	return cfg, nil
}

func Load(path string) error {
	log.Infoln("==================")
	log.Infoln("Loading real config")
	log.Infoln("==================")

	// f, err1 := os.Create("/data/data/com.github.metacubex.clash.shunf4mod.meta/cache/cpuprof_" + time.Now().Format("2006-01-02T15_04_05Z070000") + ".txt")
	// if err1 != nil {
	// 	panic(err1)
	// }
	// pprof.StartCPUProfile(f)

	rawCfg, err := UnmarshalAndPatch(path)
	if err != nil {
		log.Errorln("Load %s: %s", path, err.Error())

		return err
	}

	logDns(rawCfg)

	cfg, err := Parse(rawCfg)
	if err != nil {
		log.Errorln("Load %s: %s", path, err.Error())

		return err
	}

	executor.ApplyConfig(cfg, true)

	app.ApplySubtitlePattern(rawCfg.ClashForAndroid.UiSubtitlePattern)

	log.Infoln("==================")
	log.Infoln("Done loading real config")
	log.Infoln("==================")

	runtime.GC()

	return nil
}

func LoadDefault() {
	log.Warnln("==================")
	log.Warnln("Loading default config (resetting)")
	log.Warnln("==================")
	cfg, err := config.Parse([]byte{})
	if err != nil {
		panic(err.Error())
	}

	executor.ApplyConfig(cfg, true)
	log.Warnln("==================")
	log.Warnln("Done loading default config (resetting)")
	log.Warnln("==================")
}
