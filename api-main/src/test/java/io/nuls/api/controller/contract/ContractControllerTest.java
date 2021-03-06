package io.nuls.api.controller.contract;

import io.nuls.api.ApiModuleBootstrap;
import io.nuls.api.bean.SpringLiteContext;
import io.nuls.api.controller.model.RpcResult;
import io.nuls.api.core.util.Log;
import io.nuls.sdk.core.contast.ContractConstant;
import io.nuls.sdk.core.utils.StringUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.swing.*;

import java.io.*;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.*;

public class ContractControllerTest {

    private static String BASE;

    private static void initSys() throws Exception {
        System.setProperty("file.encoding", UTF_8.name());
        Field charset = Charset.class.getDeclaredField("defaultCharset");
        charset.setAccessible(true);
        charset.set(null, UTF_8);
    }

    private static void getBase() {
        String serverHome = System.getProperty("api.server.home");
        if(StringUtils.isBlank(serverHome)) {
            URL resource = ClassLoader.getSystemClassLoader().getResource("");
            String classPath = resource.getPath();
            File file = null;
            try {
                file = new File(URLDecoder.decode(classPath, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                Log.error(e);
                file = new File(classPath);
            }
            BASE = file.getPath();
        } else {
            BASE = serverHome;
        }
    }

    @BeforeClass
    public static void startUp() throws Exception {
        initSys();
        getBase();
        ApiModuleBootstrap.main(null);
    }

    @Test
    public void validateContractCode() {
        FileInputStream in=  null;
        try {
            TimeUnit.SECONDS.sleep(5);
            ContractController controller = SpringLiteContext.getBean(ContractController.class);
            List<Object> params = new ArrayList<>();
            String address = "TTb4Y6qzJyzHDrcNkczN7quV5mdbtMgy";
            File file = new File(BASE + "/contract/code/nrc20_clean.zip");
            in = new FileInputStream(file);
            params.add(address);
            params.add("mockHeader," + Base64.getEncoder().encodeToString(IOUtils.toByteArray(in)));
            RpcResult rpcResult = controller.validateContractCode(params);
            System.out.println(rpcResult);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(in);
        }

    }

    @Test
    public void testFileBinaryBase64Encoder() {
        FileInputStream in = null;
        OutputStream out = null;
        try {
            File file = new File(BASE + "/contract/code/ddd.zip");
            in = new FileInputStream(file);

            String fileDataURL = "aaaa,";
            fileDataURL += Base64.getEncoder().encodeToString(IOUtils.toByteArray(in));
            String[] arr = fileDataURL.split(",");
            if (arr.length != 2) {
                Assert.assertTrue(false);
            }
            String headerInfo = arr[0];
            String body = arr[1];

            //String contentType = (headerInfo.split(":")[1]).split(";")[0];
            byte[] fileContent = Base64.getDecoder().decode(body);
            out = new FileOutputStream(new File(BASE + "/contract/code/ddd_copy.zip"));
            IOUtils.write(fileContent, out);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(in);
        }
    }

    @Test
    public void getCodeTest() {
        File file = new File("/Users/pierreluo/IdeaProjects/api-module/api-main/target/test-classes/contract/code/TTbB7CA2q6QjMdiUUbLGs85nHrhvwjga/src/io/nuls/vote/contract/VoteContract.java");
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            List<String> strings = IOUtils.readLines(in);
            StringBuilder sb = new StringBuilder();
            strings.forEach(a -> {
                sb.append(a).append("\r\n");
            });
            System.out.println(sb.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(in);
        }
    }


}