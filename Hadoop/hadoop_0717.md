# HDFS
# 실습
## 하둡 실행
### sbin에 가지않고도 실행하도록 설정 
- .bash_profile에 sbin 디렉터리 추가
  
  ```
  vi .bash_profile
  ```
  
  ![image](https://user-images.githubusercontent.com/79209568/126029868-cb083627-d06b-4625-9d3c-bb15c5668485.png)
- `source .bash_profile`로 적용 후 `start-dfs.sh`로 하둡 프로세스 실행
  
  ![image](https://user-images.githubusercontent.com/79209568/126029889-bf573fcb-292f-4cfd-bef7-c2f804b08f65.png)

- 하둡 버전 확인
  
  ![image](https://user-images.githubusercontent.com/79209568/126029961-4035c3b1-f623-4614-9b6f-c667409bc9a0.png)

## 하둡 명령어
![image](https://user-images.githubusercontent.com/79209568/126029937-cbc39bdb-dfc2-4a27-a2a3-642afec637fc.png)
- 형식
  
  ```
  hadoop fs [명령어]
  ```
### 복사 (로컬 → 하둡)
#### put
#### copyFromLocal

### 복사 (하둡 → 로컬)
#### get
#### copyToLocal


<hr>


- cd /usr/local/eclipse
- ./eclipse 실행
- new - java project : hadoop
- 프로젝트 오른쪽 클릭 - build path - add library - user library - new - UserLib
- 패키지 추가 : Add External JARs
  - /usr/local/hadoop-2.10.1/share/hadoop/common/hadoop-common-2.10.1.jar
  - /usr/local/hadoop-2.10.1/share/hadoop/common/lib/commons-cli-1.2.jar
  - /usr/local/hadoop-2.10.1/share/hadoop/mapreduce/hadoop-mapreduce-client-core.2.10.1.jar
  - /usr/local/hadoop-2.10.1/share/hadoop/mapreduce/lib/log4j.1.2.17.jar

- 패키지 생성 : SinggleFileWriteRead
- 클래스 생성 : SinggleFileWriteRead
- 작성
  
  ```java
  package SingleFileWriteRead;

  import java.io.IOException;

  import org.apache.hadoop.conf.Configuration;
  import org.apache.hadoop.fs.FSDataInputStream;
  import org.apache.hadoop.fs.FSDataOutputStream;
  import org.apache.hadoop.fs.FileSystem;
  import org.apache.hadoop.fs.Path;

  public class SingleFileWriteRead {
    public static void main(String[] args) throws IOException{
      if (args.length != 2) { // args 값이 2개 일 때만
        System.out.println("usage error");
        System.exit(0);
      }

      // configuration : 하둡의 환경설정에 접근하기 위한 파일
      //  core-default.xml, core-site.xml
      Configuration conf = new Configuration(); 
      FileSystem hdfs = FileSystem.get(conf);

      Path path = new Path(args[0]); // 파일명 경로 획득 (test.txt)
      if (hdfs.exists(path)) {  // 같은 파일명 있으면 지우기
        hdfs.delete(path, true);
      }

      // 파일 쓰기
      FSDataOutputStream outputStream = hdfs.create(path);
      outputStream.writeUTF(args[1]); // 출력 값 (hello)
      outputStream.close();

      // 파일 읽기
      FSDataInputStream inputStream = hdfs.open(path);
      String result = inputStream.readUTF();
      inputStream.close();
      System.out.println("파일로부터 읽어온 내용 = "+result);
    }
  }

  ```
- 패키지에서 오른쪽 클릭 - export - JAR file
  
  ![image](https://user-images.githubusercontent.com/79209568/126032542-6acf4592-5151-4143-ba38-96ab10252ada.png)
  ![image](https://user-images.githubusercontent.com/79209568/126032547-f7677492-bd36-4626-b29c-5e2fd39a4058.png)
- 새 터미널
- `cd hadoop/eclipse-workspace` export한 jar파일이 있음
  
  ![image](https://user-images.githubusercontent.com/79209568/126032605-f255963f-f138-4801-be07-45e6ba5eaa00.png)

- `hadoop jar SingleFileWriteRead.jar SingleFileWriteRead.SingleFileWriteRead test.txt hello`
  
  ![image](https://user-images.githubusercontent.com/79209568/126032629-7d4a4db1-4497-4b10-b184-e1ed2de2930b.png)


# 실습과제

