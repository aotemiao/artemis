#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

usage() {
  cat <<'EOF' >&2
Usage: scripts/dev/new-domain-service.sh <domain> [--port <port>] [--output-root <path>] [--skip-register]

说明：
- <domain> 使用英文小写与中划线，例如：resource、workflow-engine
- 默认在当前仓库根目录生成并自动注册到 pom.xml / BOM
- 当指定 --output-root 且不是当前仓库根目录时，默认进入预览模式，必须同时加 --skip-register
EOF
}

require_cmd python3

domain=""
port="9310"
output_root=""
skip_register="0"

while (($# > 0)); do
  case "$1" in
    --port)
      port="${2:-}"
      shift 2
      ;;
    --output-root)
      output_root="${2:-}"
      shift 2
      ;;
    --skip-register)
      skip_register="1"
      shift
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      if [[ -n "$domain" ]]; then
        usage
        exit 1
      fi
      domain="$1"
      shift
      ;;
  esac
done

if [[ -z "$domain" ]]; then
  usage
  exit 1
fi

domain="$(normalize_service_name "$domain")"
if [[ ! "$domain" =~ ^[a-z][a-z0-9-]*$ ]]; then
  echo "Domain name must match: ^[a-z][a-z0-9-]*$" >&2
  exit 1
fi
if [[ ! "$port" =~ ^[0-9]{4,5}$ ]]; then
  echo "Port must be a 4-5 digit number." >&2
  exit 1
fi

repo_path="$(repo_root)"
target_root="${output_root:-$repo_path}"
mkdir -p "$target_root"
target_root="$(cd "$target_root" && pwd)"

if [[ "$target_root" != "$repo_path" && "$skip_register" != "1" ]]; then
  echo "--output-root points outside the current repository. Use --skip-register for preview mode." >&2
  exit 1
fi
if [[ "$target_root" == "$repo_path" && "$skip_register" == "1" ]]; then
  echo "--skip-register is only supported together with --output-root for preview mode." >&2
  exit 1
fi

service_artifact="artemis-${domain}"
client_artifact="${service_artifact}-client"
api_artifact="artemis-api-${domain}"
class_prefix="$(python3 - "$domain" <<'PY'
import sys
parts = sys.argv[1].split("-")
print("".join(part[:1].upper() + part[1:] for part in parts))
PY
)"
upper_snake="$(python3 - "$domain" <<'PY'
import sys
print(sys.argv[1].replace("-", "_").upper())
PY
)"
package_suffix="${domain//-/.}"
base_package="com.aotemiao.artemis.${package_suffix}"
package_path="$(printf '%s' "$base_package" | tr '.' '/')"
api_package="com.aotemiao.artemis.api.${package_suffix}"
api_package_path="$(printf '%s' "$api_package" | tr '.' '/')"
service_dir="${target_root}/artemis-modules/${service_artifact}"
api_dir="${target_root}/artemis-api/${api_artifact}"
service_api_doc="${service_dir}/SERVICE_API.md"
client_contract_doc="${service_dir}/${client_artifact}/CLIENT_CONTRACT.md"
nacos_config="${target_root}/config/nacos/${service_artifact}.yml"
run_script="${target_root}/scripts/dev/run-${domain}.sh"
readiness_script="${target_root}/scripts/dev/check-${domain}-readiness.sh"
smoke_script="${target_root}/scripts/smoke/${domain}-ping.sh"
dockerfile_path="${target_root}/docker/Dockerfile.${domain}"
service_catalog_path="${target_root}/scripts/lib/service-catalog.sh"

paths_to_create=(
  "${service_dir}"
  "${api_dir}"
  "${nacos_config}"
  "${run_script}"
  "${readiness_script}"
  "${smoke_script}"
  "${dockerfile_path}"
)

for path in "${paths_to_create[@]}"; do
  if [[ -e "$path" ]]; then
    echo "Refusing to overwrite existing path: $path" >&2
    exit 1
  fi
done

write_file() {
  local path="$1"
  mkdir -p "$(dirname "$path")"
  cat >"$path"
}

insert_before_last() {
  local file="$1"
  local marker="$2"
  local block="$3"
  python3 - "$file" "$marker" "$block" <<'PY'
from pathlib import Path
import sys

path = Path(sys.argv[1])
marker = sys.argv[2]
block = sys.argv[3]
text = path.read_text(encoding="utf-8")
if block in text:
    sys.exit(0)
index = text.rfind(marker)
if index < 0:
    raise SystemExit(f"marker not found in {path}: {marker}")
updated = text[:index] + block + "\n" + text[index:]
path.write_text(updated, encoding="utf-8")
PY
}

insert_after_first() {
  local file="$1"
  local marker="$2"
  local block="$3"
  python3 - "$file" "$marker" "$block" <<'PY'
from pathlib import Path
import sys

path = Path(sys.argv[1])
marker = sys.argv[2]
block = sys.argv[3]
text = path.read_text(encoding="utf-8")
if block in text:
    sys.exit(0)
index = text.find(marker)
if index < 0:
    raise SystemExit(f"marker not found in {path}: {marker}")
index += len(marker)
updated = text[:index] + "\n" + block + text[index:]
path.write_text(updated, encoding="utf-8")
PY
}

print_step "Generating service scaffold for ${service_artifact}"

write_file "${api_dir}/pom.xml" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.aotemiao</groupId>
        <artifactId>artemis-api</artifactId>
        <version>\${revision}</version>
        <relativePath>../pom.xml</relativePath>
    </parent>

    <artifactId>${api_artifact}</artifactId>
    <name>Artemis API ${class_prefix}</name>
    <description>Bridge module for ${service_artifact} client contracts</description>

    <dependencies>
        <dependency>
            <groupId>com.aotemiao</groupId>
            <artifactId>${client_artifact}</artifactId>
            <version>\${project.version}</version>
        </dependency>
    </dependencies>
</project>
EOF

write_file "${api_dir}/src/main/java/${api_package_path}/package-info.java" <<EOF
/**
 * ${service_artifact} 的 API bridge。
 *
 * <p>该模块只聚合 \`${client_artifact}\`，供调用方以统一入口依赖。</p>
 */
package ${api_package};
EOF

write_file "${service_dir}/pom.xml" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.aotemiao</groupId>
        <artifactId>artemis-modules</artifactId>
        <version>\${revision}</version>
        <relativePath>../pom.xml</relativePath>
    </parent>

    <artifactId>${service_artifact}</artifactId>
    <packaging>pom</packaging>
    <name>Artemis ${class_prefix}</name>
    <description>${class_prefix} domain service scaffold</description>

    <modules>
        <module>${client_artifact}</module>
        <module>${service_artifact}-domain</module>
        <module>${service_artifact}-infra</module>
        <module>${service_artifact}-app</module>
        <module>${service_artifact}-adapter</module>
        <module>${service_artifact}-start</module>
    </modules>
</project>
EOF

write_file "${service_dir}/${client_artifact}/pom.xml" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.aotemiao</groupId>
        <artifactId>${service_artifact}</artifactId>
        <version>\${revision}</version>
        <relativePath>../pom.xml</relativePath>
    </parent>

    <artifactId>${client_artifact}</artifactId>
    <name>${class_prefix} Client</name>
    <description>Dubbo interface and DTO for ${service_artifact}</description>

    <dependencies>
        <dependency>
            <groupId>org.apache.dubbo</groupId>
            <artifactId>dubbo</artifactId>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>jakarta.validation</groupId>
            <artifactId>jakarta.validation-api</artifactId>
        </dependency>
    </dependencies>
</project>
EOF

write_file "${service_dir}/${client_artifact}/src/main/java/${package_path}/client/api/${class_prefix}PingService.java" <<EOF
package ${base_package}.client.api;

import ${base_package}.client.dto.PingResponse;

/** ${service_artifact} 对内 ping 契约。 */
public interface ${class_prefix}PingService {

    /** 返回当前服务模板的最小状态。 */
    PingResponse ping();
}
EOF

write_file "${service_dir}/${client_artifact}/src/main/java/${package_path}/client/dto/PingResponse.java" <<EOF
package ${base_package}.client.dto;

/** ${service_artifact} 的最小状态 DTO。 */
public record PingResponse(String serviceCode, String message) {}
EOF

write_file "${client_contract_doc}" <<EOF
# ${class_prefix} Client Contract

Status: maintained
Last Reviewed: 2026-03-24
Review Cadence: 90 days

- \`INTERFACE: ${base_package}.client.api.${class_prefix}PingService\`
- \`METHOD: PingResponse ping()\`
- \`DTO: ${base_package}.client.dto.PingResponse(String serviceCode, String message)\`
EOF

write_file "${service_dir}/${service_artifact}-domain/pom.xml" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.aotemiao</groupId>
        <artifactId>${service_artifact}</artifactId>
        <version>\${revision}</version>
        <relativePath>../pom.xml</relativePath>
    </parent>

    <artifactId>${service_artifact}-domain</artifactId>
    <name>${class_prefix} Domain</name>
    <description>Domain layer for ${service_artifact}</description>
</project>
EOF

write_file "${service_dir}/${service_artifact}-domain/src/main/java/${package_path}/domain/model/ServicePing.java" <<EOF
package ${base_package}.domain.model;

/** ${service_artifact} 的最小领域状态。 */
public record ServicePing(String serviceCode, String message) {}
EOF

write_file "${service_dir}/${service_artifact}-domain/src/main/java/${package_path}/domain/gateway/${class_prefix}PingGateway.java" <<EOF
package ${base_package}.domain.gateway;

import ${base_package}.domain.model.ServicePing;

/** ${service_artifact} 读取最小状态的领域网关。 */
public interface ${class_prefix}PingGateway {

    /** 返回服务模板的最小可用状态。 */
    ServicePing loadPing();
}
EOF

write_file "${service_dir}/${service_artifact}-app/pom.xml" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.aotemiao</groupId>
        <artifactId>${service_artifact}</artifactId>
        <version>\${revision}</version>
        <relativePath>../pom.xml</relativePath>
    </parent>

    <artifactId>${service_artifact}-app</artifactId>
    <name>${class_prefix} App</name>
    <description>Application layer for ${service_artifact}</description>

    <dependencies>
        <dependency>
            <groupId>com.aotemiao</groupId>
            <artifactId>${service_artifact}-domain</artifactId>
            <version>\${project.version}</version>
        </dependency>
        <dependency>
            <groupId>com.aotemiao</groupId>
            <artifactId>artemis-framework-core</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-tx</artifactId>
        </dependency>
        <dependency>
            <groupId>com.github.spotbugs</groupId>
            <artifactId>spotbugs-annotations</artifactId>
            <version>4.8.3</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.mockito</groupId>
            <artifactId>mockito-junit-jupiter</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.assertj</groupId>
            <artifactId>assertj-core</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.jacoco</groupId>
                <artifactId>jacoco-maven-plugin</artifactId>
                <executions>
                    <execution>
                        <id>jacoco-prepare-agent</id>
                        <goals>
                            <goal>prepare-agent</goal>
                        </goals>
                    </execution>
                    <execution>
                        <id>jacoco-report</id>
                        <phase>verify</phase>
                        <goals>
                            <goal>report</goal>
                        </goals>
                    </execution>
                    <execution>
                        <id>jacoco-check</id>
                        <phase>verify</phase>
                        <goals>
                            <goal>check</goal>
                        </goals>
                        <configuration>
                            <rules>
                                <rule>
                                    <element>BUNDLE</element>
                                    <limits>
                                        <limit>
                                            <counter>LINE</counter>
                                            <value>COVEREDRATIO</value>
                                            <minimum>0.10</minimum>
                                        </limit>
                                    </limits>
                                </rule>
                            </rules>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
EOF

write_file "${service_dir}/${service_artifact}-app/src/main/java/${package_path}/app/query/Get${class_prefix}PingQryExe.java" <<EOF
package ${base_package}.app.query;

import ${base_package}.domain.gateway.${class_prefix}PingGateway;
import ${base_package}.domain.model.ServicePing;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;

/** 读取 ${service_artifact} 最小状态的查询执行器。 */
@Component
public class Get${class_prefix}PingQryExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this query executor does not expose it.")
    private final ${class_prefix}PingGateway pingGateway;

    public Get${class_prefix}PingQryExe(${class_prefix}PingGateway pingGateway) {
        this.pingGateway = pingGateway;
    }

    /** 返回服务最小状态。 */
    public ServicePing execute() {
        return pingGateway.loadPing();
    }
}
EOF

write_file "${service_dir}/${service_artifact}-app/src/test/java/${package_path}/app/query/Get${class_prefix}PingQryExeTest.java" <<EOF
package ${base_package}.app.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ${base_package}.domain.gateway.${class_prefix}PingGateway;
import ${base_package}.domain.model.ServicePing;
import org.junit.jupiter.api.Test;

class Get${class_prefix}PingQryExeTest {

    @Test
    void execute_should_delegate_to_gateway() {
        ${class_prefix}PingGateway pingGateway = mock(${class_prefix}PingGateway.class);
        when(pingGateway.loadPing()).thenReturn(new ServicePing("${service_artifact}", "Service scaffold is ready"));

        Get${class_prefix}PingQryExe exe = new Get${class_prefix}PingQryExe(pingGateway);

        ServicePing result = exe.execute();

        assertThat(result.serviceCode()).isEqualTo("${service_artifact}");
        assertThat(result.message()).isEqualTo("Service scaffold is ready");
    }
}
EOF

write_file "${service_dir}/${service_artifact}-infra/pom.xml" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.aotemiao</groupId>
        <artifactId>${service_artifact}</artifactId>
        <version>\${revision}</version>
        <relativePath>../pom.xml</relativePath>
    </parent>

    <artifactId>${service_artifact}-infra</artifactId>
    <name>${class_prefix} Infra</name>
    <description>Infrastructure layer for ${service_artifact}</description>

    <dependencies>
        <dependency>
            <groupId>com.aotemiao</groupId>
            <artifactId>${service_artifact}-domain</artifactId>
            <version>\${project.version}</version>
        </dependency>
        <dependency>
            <groupId>com.aotemiao</groupId>
            <artifactId>artemis-framework-jdbc</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.jacoco</groupId>
                <artifactId>jacoco-maven-plugin</artifactId>
                <executions>
                    <execution>
                        <id>jacoco-prepare-agent</id>
                        <goals>
                            <goal>prepare-agent</goal>
                        </goals>
                    </execution>
                    <execution>
                        <id>jacoco-report</id>
                        <phase>verify</phase>
                        <goals>
                            <goal>report</goal>
                        </goals>
                    </execution>
                    <execution>
                        <id>jacoco-check</id>
                        <phase>verify</phase>
                        <goals>
                            <goal>check</goal>
                        </goals>
                        <configuration>
                            <rules>
                                <rule>
                                    <element>BUNDLE</element>
                                    <limits>
                                        <limit>
                                            <counter>LINE</counter>
                                            <value>COVEREDRATIO</value>
                                            <minimum>0.10</minimum>
                                        </limit>
                                    </limits>
                                </rule>
                            </rules>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
EOF

write_file "${service_dir}/${service_artifact}-infra/src/main/java/${package_path}/infra/gateway/${class_prefix}PingGatewayImpl.java" <<EOF
package ${base_package}.infra.gateway;

import ${base_package}.domain.gateway.${class_prefix}PingGateway;
import ${base_package}.domain.model.ServicePing;
import org.springframework.stereotype.Component;

/** ${service_artifact} 最小状态网关实现。 */
@Component
public class ${class_prefix}PingGatewayImpl implements ${class_prefix}PingGateway {

    @Override
    public ServicePing loadPing() {
        return new ServicePing("${service_artifact}", "Service scaffold is ready");
    }
}
EOF

write_file "${service_dir}/${service_artifact}-infra/src/test/java/${package_path}/infra/gateway/${class_prefix}PingGatewayImplTest.java" <<EOF
package ${base_package}.infra.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ${class_prefix}PingGatewayImplTest {

    @Test
    void loadPing_should_return_default_payload() {
        ${class_prefix}PingGatewayImpl gateway = new ${class_prefix}PingGatewayImpl();

        var result = gateway.loadPing();

        assertThat(result.serviceCode()).isEqualTo("${service_artifact}");
        assertThat(result.message()).isEqualTo("Service scaffold is ready");
    }
}
EOF

write_file "${service_dir}/${service_artifact}-adapter/pom.xml" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.aotemiao</groupId>
        <artifactId>${service_artifact}</artifactId>
        <version>\${revision}</version>
        <relativePath>../pom.xml</relativePath>
    </parent>

    <artifactId>${service_artifact}-adapter</artifactId>
    <name>${class_prefix} Adapter</name>
    <description>Adapter layer for ${service_artifact}</description>

    <dependencies>
        <dependency>
            <groupId>com.aotemiao</groupId>
            <artifactId>${service_artifact}-app</artifactId>
            <version>\${project.version}</version>
        </dependency>
        <dependency>
            <groupId>com.aotemiao</groupId>
            <artifactId>${client_artifact}</artifactId>
            <version>\${project.version}</version>
        </dependency>
        <dependency>
            <groupId>org.apache.dubbo</groupId>
            <artifactId>dubbo</artifactId>
        </dependency>
        <dependency>
            <groupId>com.aotemiao</groupId>
            <artifactId>artemis-framework-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.jacoco</groupId>
                <artifactId>jacoco-maven-plugin</artifactId>
                <executions>
                    <execution>
                        <id>jacoco-prepare-agent</id>
                        <goals>
                            <goal>prepare-agent</goal>
                        </goals>
                    </execution>
                    <execution>
                        <id>jacoco-report</id>
                        <phase>verify</phase>
                        <goals>
                            <goal>report</goal>
                        </goals>
                    </execution>
                    <execution>
                        <id>jacoco-check</id>
                        <phase>verify</phase>
                        <goals>
                            <goal>check</goal>
                        </goals>
                        <configuration>
                            <rules>
                                <rule>
                                    <element>BUNDLE</element>
                                    <limits>
                                        <limit>
                                            <counter>LINE</counter>
                                            <value>COVEREDRATIO</value>
                                            <minimum>0.10</minimum>
                                        </limit>
                                    </limits>
                                </rule>
                            </rules>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
EOF

write_file "${service_dir}/${service_artifact}-adapter/src/main/java/${package_path}/adapter/web/${class_prefix}PingController.java" <<EOF
package ${base_package}.adapter.web;

import ${base_package}.app.query.Get${class_prefix}PingQryExe;
import ${base_package}.client.dto.PingResponse;
import com.aotemiao.artemis.framework.core.domain.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** ${service_artifact} 对外 ping 接口。 */
@RestController
@RequestMapping(${class_prefix}PingController.BASE_PATH)
public class ${class_prefix}PingController {

    public static final String BASE_PATH = "/api/${domain}/ping";

    private final Get${class_prefix}PingQryExe get${class_prefix}PingQryExe;

    public ${class_prefix}PingController(Get${class_prefix}PingQryExe get${class_prefix}PingQryExe) {
        this.get${class_prefix}PingQryExe = get${class_prefix}PingQryExe;
    }

    /** 返回服务模板的最小状态。 */
    @GetMapping
    public R<PingResponse> ping() {
        var ping = get${class_prefix}PingQryExe.execute();
        return R.ok(new PingResponse(ping.serviceCode(), ping.message()));
    }
}
EOF

write_file "${service_dir}/${service_artifact}-adapter/src/main/java/${package_path}/adapter/dubbo/${class_prefix}PingServiceDubboImpl.java" <<EOF
package ${base_package}.adapter.dubbo;

import ${base_package}.app.query.Get${class_prefix}PingQryExe;
import ${base_package}.client.api.${class_prefix}PingService;
import ${base_package}.client.dto.PingResponse;
import org.apache.dubbo.config.annotation.DubboService;

/** ${service_artifact} 的 Dubbo ping 实现。 */
@DubboService
public class ${class_prefix}PingServiceDubboImpl implements ${class_prefix}PingService {

    private final Get${class_prefix}PingQryExe get${class_prefix}PingQryExe;

    public ${class_prefix}PingServiceDubboImpl(Get${class_prefix}PingQryExe get${class_prefix}PingQryExe) {
        this.get${class_prefix}PingQryExe = get${class_prefix}PingQryExe;
    }

    @Override
    public PingResponse ping() {
        var ping = get${class_prefix}PingQryExe.execute();
        return new PingResponse(ping.serviceCode(), ping.message());
    }
}
EOF

write_file "${service_dir}/${service_artifact}-adapter/src/test/java/${package_path}/adapter/web/${class_prefix}PingControllerTest.java" <<EOF
package ${base_package}.adapter.web;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ${base_package}.app.query.Get${class_prefix}PingQryExe;
import ${base_package}.domain.model.ServicePing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ${class_prefix}PingControllerTest {

    private MockMvc mockMvc;

    private Get${class_prefix}PingQryExe get${class_prefix}PingQryExe;

    @BeforeEach
    void setUp() {
        get${class_prefix}PingQryExe = mock(Get${class_prefix}PingQryExe.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new ${class_prefix}PingController(get${class_prefix}PingQryExe))
                .build();
    }

    @Test
    void ping_should_return_payload() throws Exception {
        when(get${class_prefix}PingQryExe.execute())
                .thenReturn(new ServicePing("${service_artifact}", "Service scaffold is ready"));

        mockMvc.perform(get(${class_prefix}PingController.BASE_PATH))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.serviceCode").value("${service_artifact}"))
                .andExpect(jsonPath("$.data.message").value("Service scaffold is ready"));
    }
}
EOF

write_file "${service_api_doc}" <<EOF
# ${class_prefix} Service API

Status: maintained
Last Reviewed: 2026-03-24
Review Cadence: 90 days

- \`ROUTE: GET /api/${domain}/ping\`
- 用途：验证 \`${service_artifact}\` 已经完成最小服务装配
- 返回：\`serviceCode\` 与 \`message\`
EOF

write_file "${service_dir}/${service_artifact}-start/pom.xml" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.aotemiao</groupId>
        <artifactId>${service_artifact}</artifactId>
        <version>\${revision}</version>
        <relativePath>../pom.xml</relativePath>
    </parent>

    <artifactId>${service_artifact}-start</artifactId>
    <name>${class_prefix} Start</name>
    <description>Bootstrap module for ${service_artifact}</description>

    <dependencies>
        <dependency>
            <groupId>com.aotemiao</groupId>
            <artifactId>${service_artifact}-adapter</artifactId>
            <version>\${project.version}</version>
        </dependency>
        <dependency>
            <groupId>com.aotemiao</groupId>
            <artifactId>${client_artifact}</artifactId>
            <version>\${project.version}</version>
        </dependency>
        <dependency>
            <groupId>com.aotemiao</groupId>
            <artifactId>${service_artifact}-infra</artifactId>
            <version>\${project.version}</version>
        </dependency>
        <dependency>
            <groupId>org.apache.dubbo</groupId>
            <artifactId>dubbo-spring-boot-starter</artifactId>
        </dependency>
        <dependency>
            <groupId>org.apache.dubbo</groupId>
            <artifactId>dubbo-registry-nacos</artifactId>
        </dependency>
        <dependency>
            <groupId>com.aotemiao</groupId>
            <artifactId>artemis-framework-web</artifactId>
        </dependency>
        <dependency>
            <groupId>com.aotemiao</groupId>
            <artifactId>artemis-framework-jdbc</artifactId>
        </dependency>
        <dependency>
            <groupId>com.aotemiao</groupId>
            <artifactId>artemis-framework-redis</artifactId>
        </dependency>
        <dependency>
            <groupId>com.aotemiao</groupId>
            <artifactId>artemis-framework-doc</artifactId>
        </dependency>
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
        </dependency>
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-database-postgresql</artifactId>
        </dependency>
        <dependency>
            <groupId>com.tngtech.archunit</groupId>
            <artifactId>archunit</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <executions>
                    <execution>
                        <id>repackage</id>
                        <goals>
                            <goal>repackage</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
EOF

write_file "${service_dir}/${service_artifact}-start/src/main/java/${package_path}/${class_prefix}Application.java" <<EOF
package ${base_package};

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** ${service_artifact} 启动入口。 */
@SpringBootApplication
public class ${class_prefix}Application {

    public static void main(String[] args) {
        SpringApplication.run(${class_prefix}Application.class, args);
    }
}
EOF

write_file "${service_dir}/${service_artifact}-start/src/test/java/${package_path}/arch/${class_prefix}LayerDependencyRulesTest.java" <<EOF
package ${base_package}.arch;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import org.junit.jupiter.api.Test;

/** ${service_artifact} 分层依赖规则。 */
class ${class_prefix}LayerDependencyRulesTest {

    private final JavaClasses importedClasses = new ClassFileImporter().importPackages("${base_package}");

    @Test
    void adapter_should_not_depend_on_infra() {
        ArchRuleDefinition.noClasses()
                .that()
                .resideInAPackage("${base_package}.adapter..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("${base_package}.infra..")
                .check(importedClasses);
    }

    @Test
    void app_should_not_depend_on_infra() {
        ArchRuleDefinition.noClasses()
                .that()
                .resideInAPackage("${base_package}.app..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("${base_package}.infra..")
                .check(importedClasses);
    }

    @Test
    void domain_should_not_depend_on_other_internal_layers() {
        ArchRuleDefinition.noClasses()
                .that()
                .resideInAPackage("${base_package}.domain..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        "${base_package}.adapter..",
                        "${base_package}.app..",
                        "${base_package}.infra..")
                .check(importedClasses);
    }

    @Test
    void infra_should_not_depend_on_adapter_or_app() {
        ArchRuleDefinition.noClasses()
                .that()
                .resideInAPackage("${base_package}.infra..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("${base_package}.adapter..", "${base_package}.app..")
                .check(importedClasses);
    }

    @Test
    void client_should_not_depend_on_internal_layers() {
        ArchRuleDefinition.noClasses()
                .that()
                .resideInAPackage("${base_package}.client..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        "${base_package}.adapter..",
                        "${base_package}.app..",
                        "${base_package}.domain..",
                        "${base_package}.infra..",
                        "${base_package}.start..")
                .check(importedClasses);
    }
}
EOF

write_file "${service_dir}/${service_artifact}-start/src/main/resources/application.yml" <<EOF
# Tomcat 配置
server:
  port: ${port}

# Spring 配置
spring:
  application:
    name: ${service_artifact}
  profiles:
    active: @profiles.active@

# Dubbo：与 Nacos 共用注册中心，应用名与 spring.application.name 一致
dubbo:
  application:
    name: ${service_artifact}
  registry:
    address: nacos://\${spring.cloud.nacos.server-addr:127.0.0.1:8848}

# Spring Data JDBC 仓库扫描包（Ant 风格：com.aotemiao.artemis.*.infra.repository 覆盖所有业务模块）
artemis:
  jdbc:
    repositories:
      base-packages: com.aotemiao.artemis.*.infra.repository

--- # nacos 配置
spring:
  cloud:
    nacos:
      server-addr: @nacos.server@
      username: @nacos.username@
      password: @nacos.password@
      discovery:
        group: @nacos.discovery.group@
        namespace: \${spring.profiles.active}
      config:
        group: @nacos.config.group@
        namespace: \${spring.profiles.active}
  config:
    import:
      - optional:nacos:application-common.yml
      - optional:nacos:datasource.yml
      - optional:nacos:${service_artifact}.yml
EOF

write_file "${service_dir}/${service_artifact}-start/src/main/resources/logback-plus.xml" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<configuration scan="true" scanPeriod="60 seconds" debug="false">
    <!-- 日志存放路径与文件名，保持与 scripts/dev/tail-log.sh 一致 -->
    <springProperty name="APP_NAME" source="spring.application.name" defaultValue="${service_artifact}"/>
    <property name="LOG_PATH" value="./logs" />
    <property name="LOG_FILE" value="\${LOG_PATH}/\${APP_NAME}.log" />
    <property name="console.log.pattern"
              value="%cyan(%d{yyyy-MM-dd HH:mm:ss}) %green([%thread]) %highlight(%-5level) %boldMagenta(%logger{36}) - %msg%n"/>
    <property name="file.log.pattern"
              value="%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"/>

    <appender name="console" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>\${console.log.pattern}</pattern>
            <charset>utf-8</charset>
        </encoder>
    </appender>

    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>\${LOG_FILE}</file>
        <encoder>
            <pattern>\${file.log.pattern}</pattern>
            <charset>UTF-8</charset>
        </encoder>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>\${LOG_PATH}/\${APP_NAME}-%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <maxFileSize>100MB</maxFileSize>
            <maxHistory>15</maxHistory>
            <totalSizeCap>20GB</totalSizeCap>
        </rollingPolicy>
    </appender>

    <include resource="logback-common.xml" />
    <include resource="logback-logstash.xml" />
    <include resource="logback-skylog.xml" />

    <root level="info">
        <appender-ref ref="console" />
        <appender-ref ref="FILE" />
    </root>
</configuration>
EOF

write_file "${nacos_config}" <<EOF
# ${service_artifact} 专属配置模板
springdoc:
  api-docs:
    enabled: true
  swagger-ui:
    enabled: true

logging:
  level:
    ${base_package}: INFO
EOF

write_file "${run_script}" <<EOF
#!/usr/bin/env bash

set -euo pipefail

source "\$(cd "\$(dirname "\${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

run_in_repo_root

print_step "Starting ${service_artifact}"
run_packaged_service_module artemis-modules/${service_artifact}/${service_artifact}-start "\$@"
EOF

write_file "${readiness_script}" <<EOF
#!/usr/bin/env bash

set -euo pipefail

source "\$(cd "\$(dirname "\${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

run_in_repo_root

scripts/dev/check-service-config.sh "${domain}"
scripts/smoke/${domain}-ping.sh "\${1:-http://127.0.0.1:${port}}"
EOF

write_file "${smoke_script}" <<EOF
#!/usr/bin/env bash

set -euo pipefail

source "\$(cd "\$(dirname "\${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

run_in_repo_root

base_url="\${1:-http://127.0.0.1:${port}}"
ping_url="\${base_url}/api/${domain}/ping"

print_step "Smoke checking ${service_artifact} ping endpoint"
scripts/dev/wait-http.sh "\${ping_url}" "200" 20 1 "GET"

print_step "${service_artifact} ping smoke completed"
EOF

write_file "${dockerfile_path}" <<EOF
ARG JAVA_VERSION=21
FROM eclipse-temurin:\${JAVA_VERSION}-jre-alpine AS runtime

ARG JAR_PATH=artemis-modules/${service_artifact}/${service_artifact}-start/target/*.jar
COPY \${JAR_PATH} app.jar

EXPOSE ${port}
ENTRYPOINT ["java", "-jar", "/app.jar"]
EOF

chmod +x "${run_script}" "${readiness_script}" "${smoke_script}"

if [[ "$skip_register" != "1" ]]; then
  print_step "Registering ${service_artifact} into repository aggregators"

  insert_before_last \
    "${repo_path}/artemis-modules/pom.xml" \
    "    </modules>" \
    "        <module>${service_artifact}</module>"

  insert_before_last \
    "${repo_path}/artemis-api/pom.xml" \
    "    </modules>" \
    "        <module>${api_artifact}</module>"

  insert_before_last \
    "${repo_path}/artemis-api/artemis-api-bom/pom.xml" \
    "        </dependencies>" \
"            <dependency>
                <groupId>com.aotemiao</groupId>
                <artifactId>${api_artifact}</artifactId>
                <version>\${project.version}</version>
            </dependency>
            <dependency>
                <groupId>com.aotemiao</groupId>
                <artifactId>${client_artifact}</artifactId>
                <version>\${project.version}</version>
            </dependency>"

  insert_after_first \
    "${repo_path}/artemis-dependencies/pom.xml" \
    "            <!-- Artemis 内部模块 -->" \
"            <dependency>
                <groupId>com.aotemiao</groupId>
                <artifactId>${api_artifact}</artifactId>
                <version>\${revision}</version>
            </dependency>
            <dependency>
                <groupId>com.aotemiao</groupId>
                <artifactId>${client_artifact}</artifactId>
                <version>\${revision}</version>
            </dependency>"

  insert_before_last \
    "${service_catalog_path}" \
    "  # -- end generated service records --" \
"  \"${domain}|domain|artemis-modules/${service_artifact}/${service_artifact}-start|${port}|config/nacos/application-common.yml,config/nacos/datasource.yml,config/nacos/${service_artifact}.yml|smoke||||scripts/smoke/${domain}-ping.sh|logs/${service_artifact}.log|docker/Dockerfile.${domain}|${api_artifact}\""
fi

print_step "Domain service scaffold generated"
echo "Service module: ${service_dir}"
echo "API bridge: ${api_dir}"
echo "Run script: ${run_script}"
echo "Smoke script: ${smoke_script}"
