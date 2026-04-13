nexacro-xapi17.jar 배치 방법:
1. Nexacro 17 설치 경로의 tool/server_lib/ 에서 nexacro-xapi17.jar 복사
2. 이 libs/ 디렉토리에 저장
3. pom.xml에 system scope dependency 추가:
   <dependency>
       <groupId>com.nexacro</groupId>
       <artifactId>xapi</artifactId>
       <version>17.0</version>
       <scope>system</scope>
       <systemPath>${project.basedir}/libs/nexacro-xapi17.jar</systemPath>
   </dependency>
4. 실제 jar 사용 시 com/nexacro/xapi/ 하위 스텁 클래스 삭제

현재는 스텁 클래스(src/main/java/com/nexacro/)로 대체 운영 중.
