# 版本升级
version=1.2.1

# 以下不动
template=$(cat << EOF
package com.tangzc.autotable.core.constants;

public interface Version {
    String VALUE = "${version}";
}
EOF
)

echo "开始替换Version.java的版本号：${version}"
echo ${template} > ./auto-table-core/src/main/java/com/tangzc/autotable/core/constants/Version.java

echo "开始替换pom.xml的版本号：${version}"
mvn versions:set -DnewVersion=${version}

echo "开始commit到本地仓库：${version}"
git commit -am "版本升级：${version}"

echo "开始打tag：v${version}"
git tag -a v${version} -m "版本号：${version}"

echo "开始提交到远程git仓库：${version}"
git push origin main --tags

echo "开始发布新的版本到maven仓库：${version}"
mvn clean deploy -Dmaven.test.skip=true -pl !auto-table-test