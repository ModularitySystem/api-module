/*
 * MIT License
 * Copyright (c) 2017-2019 nuls.io
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.nuls.api.controller.contract;

import io.nuls.api.bean.annotation.Autowired;
import io.nuls.api.bean.annotation.Controller;
import io.nuls.api.bean.annotation.RpcMethod;
import io.nuls.api.bridge.WalletRPCHandler;
import io.nuls.api.controller.model.RpcErrorCode;
import io.nuls.api.controller.model.RpcResult;
import io.nuls.api.controller.model.RpcResultError;
import io.nuls.api.controller.utils.VerifyUtils;
import io.nuls.api.core.model.*;
import io.nuls.api.core.util.Log;
import io.nuls.api.service.ContractService;
import io.nuls.api.service.TokenService;
import io.nuls.api.utils.JsonRpcException;
import io.nuls.api.utils.RunShellUtil;
import io.nuls.sdk.core.model.CreateContractData;
import io.nuls.sdk.core.model.Result;
import io.nuls.sdk.core.model.transaction.CreateContractTransaction;
import io.nuls.sdk.core.utils.AddressTool;
import io.nuls.contract.validation.service.CompareJar;
import io.nuls.sdk.core.utils.StringUtils;
import io.nuls.sdk.tool.NulsSDKTool;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Niels
 */
@Controller
public class ContractController {

    private static String BASE;

    private static String VALIDATE_HOME;

    static {
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
        VALIDATE_HOME = BASE + File.separator + "contract" + File.separator + "code" + File.separator;
    }

    @Autowired
    private WalletRPCHandler rpcHandler;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private ContractService contractService;

    @RpcMethod("getAccountTokens")
    public RpcResult getAccountTokens(List<Object> params) {
        VerifyUtils.verifyParams(params, 3);
        int pageIndex = (int) params.get(0);
        int pageSize = (int) params.get(1);
        String address = (String) params.get(2);
        if (!AddressTool.validAddress(address)) {
            throw new JsonRpcException(new RpcResultError(RpcErrorCode.PARAMS_ERROR, "[address] is inValid"));
        }
        if (pageIndex <= 0) {
            pageIndex = 1;
        }
        if (pageSize <= 0 || pageSize > 100) {
            pageSize = 10;
        }
        PageInfo<AccountTokenInfo> pageInfo = tokenService.getAccountTokens(address, pageIndex, pageSize);
        RpcResult result = new RpcResult();
        result.setResult(pageInfo);
        return result;
    }

    @RpcMethod("getTokenTransfers")
    public RpcResult getTokenTransfers(List<Object> params) {
        VerifyUtils.verifyParams(params, 4);
        int pageIndex = (int) params.get(0);
        int pageSize = (int) params.get(1);
        String address = (String) params.get(2);
        String contractAddress = (String) params.get(3);

        if (!AddressTool.validAddress(address)) {
            throw new JsonRpcException(new RpcResultError(RpcErrorCode.PARAMS_ERROR, "[address] is inValid"));
        }
        if (pageIndex <= 0) {
            pageIndex = 1;
        }
        if (pageSize <= 0 || pageSize > 100) {
            pageSize = 10;
        }
        PageInfo<TokenTransfer> pageInfo = tokenService.getTokenTransfers(address, contractAddress, pageIndex, pageSize);
        RpcResult result = new RpcResult();
        result.setResult(pageInfo);
        return result;

    }

    @RpcMethod("getContract")
    public RpcResult getContract(List<Object> params) {
        VerifyUtils.verifyParams(params, 1);
        String contractAddress = (String) params.get(0);
        if (!AddressTool.validAddress(contractAddress)) {
            throw new JsonRpcException(new RpcResultError(RpcErrorCode.PARAMS_ERROR, "[contractAddress] is inValid"));
        }
        RpcResult rpcResult = new RpcResult();
        try {
            ContractInfo contractInfo = contractService.getContractInfo(contractAddress);
            if (contractInfo == null) {
                rpcResult.setError(new RpcResultError(RpcErrorCode.DATA_NOT_EXISTS));
            } else {
                rpcResult.setResult(contractInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rpcResult;
    }

    @RpcMethod("getContractTxList")
    public RpcResult getContractTxList(List<Object> params) {
        VerifyUtils.verifyParams(params, 4);
        int pageIndex = (int) params.get(0);
        int pageSize = (int) params.get(1);
        int type = (int) params.get(2);
        String contractAddress = (String) params.get(3);

        if (!AddressTool.validAddress(contractAddress)) {
            throw new JsonRpcException(new RpcResultError(RpcErrorCode.PARAMS_ERROR, "[contractAddress] is inValid"));
        }
        if (pageIndex <= 0) {
            pageIndex = 1;
        }
        if (pageSize <= 0 || pageSize > 100) {
            pageSize = 10;
        }
        PageInfo<ContractTxInfo> pageInfo = contractService.getContractTxList(contractAddress, type, pageIndex, pageSize);
        RpcResult result = new RpcResult();
        result.setResult(pageInfo);
        return result;
    }

    @RpcMethod("getContractList")
    public RpcResult getContractList(List<Object> params) {
        VerifyUtils.verifyParams(params, 3);
        int pageIndex = (int) params.get(0);
        int pageSize = (int) params.get(1);
        boolean onlyNrc20 = (boolean) params.get(2);
        boolean isHidden = (boolean) params.get(3);
        if (pageIndex <= 0) {
            pageIndex = 1;
        }
        if (pageSize <= 0 || pageSize > 100) {
            pageSize = 10;
        }
        PageInfo<ContractInfo> pageInfo = contractService.getContractList(pageIndex, pageSize, onlyNrc20, isHidden);
        RpcResult result = new RpcResult();
        result.setResult(pageInfo);
        return result;
    }

    @RpcMethod("validateContractCode")
    public RpcResult validateContractCode(List<Object> params) {
        RpcResult result = new RpcResult();
        OutputStream out = null;
        InputStream jarIn = null;
        try {
            VerifyUtils.verifyParams(params, 2);
            String contractAddress = (String) params.get(0);
            if (!AddressTool.validAddress(contractAddress)) {
                result.setError(new RpcResultError(RpcErrorCode.PARAMS_ERROR, "[contractAddress] is inValid"));
                return result;
            }

            // 生成文件
            String fileDataURL = (String) params.get(1);
            String[] arr = fileDataURL.split(",");
            if (arr.length != 2) {
                result.setError(new RpcResultError(RpcErrorCode.PARAMS_ERROR));
                return result;
            }
            String headerInfo = arr[0];
            String body = arr[1];
            byte[] fileContent = Base64.getDecoder().decode(body);
            out = new FileOutputStream(VALIDATE_HOME + contractAddress +".zip");
            IOUtils.write(fileContent, out);

            // 编译代码
            List<String> resultList = RunShellUtil.run(BASE + File.separator + "bin" + File.separator + "compile.sh", contractAddress);
            if(!resultList.isEmpty()) {
                String error = resultList.stream().collect(Collectors.joining());
                Log.error(error);
                result.setError(new RpcResultError(RpcErrorCode.TX_SHELL_ERROR));
                return result;
            }
            File jarFile = new File(VALIDATE_HOME + contractAddress + File.separator + contractAddress +".jar");
            jarIn = new FileInputStream(jarFile);
            byte[] validateContractCode = IOUtils.toByteArray(jarIn);

            // 获取智能合约的代码
            List<Object> paras = new ArrayList<>();
            paras.add(contractAddress);
            RpcResult contract = getContract(paras);
            Object obj = contract.getResult();
            if(obj == null) {
                result.setError(new RpcResultError(RpcErrorCode.DATA_NOT_EXISTS));
                return result;
            }
            ContractInfo contractInfo = (ContractInfo) obj;
            String createTxHash = contractInfo.getCreateTxHash();
            Result result1 = NulsSDKTool.getTxWithBytes(createTxHash);
            if (result1.isFailed()) {
                result.setError(new RpcResultError(RpcErrorCode.DATA_NOT_EXISTS));
                return result;
            }
            CreateContractTransaction tx = (CreateContractTransaction) result1.getData();
            CreateContractData txData = tx.getTxData();
            byte[] contractCode = txData.getCode();

            // 比较代码指令
            boolean bool = CompareJar.compareJarBytes(contractCode, validateContractCode);
            result.setResult(bool);
        } catch (Exception e) {
            Log.error(e);
            result.setError(new RpcResultError(RpcErrorCode.PARAMS_ERROR, e.getMessage()));
        } finally {
            IOUtils.closeQuietly(jarIn);
            IOUtils.closeQuietly(out);
        }
        return result;
    }

    @RpcMethod("getContractCodeTree")
    public RpcResult getContractCodeTree(List<Object> params) {
        RpcResult result = new RpcResult();
        VerifyUtils.verifyParams(params, 1);
        String contractAddress = (String) params.get(0);
        if (!AddressTool.validAddress(contractAddress)) {
            result.setError(new RpcResultError(RpcErrorCode.PARAMS_ERROR, "[contractAddress] is inValid"));
            return result;
        }
        //TODO 检查认证状态，通过认证的合约继续下一步


        File src = new File(VALIDATE_HOME + contractAddress + File.separator + "src");
        ContractCode root = new ContractCode();
        ContractCodeNode rootNode = new ContractCodeNode();
        if(!src.isDirectory()) {
            result.setError(new RpcResultError(RpcErrorCode.PARAMS_ERROR, "root path is inValid"));
            return result;
        }
        List<ContractCodeNode> children = new ArrayList<>();
        rootNode.setName(src.getName());
        rootNode.setPath(extractFilePath(src));
        rootNode.setDir(true);
        rootNode.setChildren(children);
        root.setRoot(rootNode);
        File[] files = src.listFiles();
        recursive(src.listFiles(), children);

        result.setResult(root);
        return result;
    }

    private void recursive(File[] files, List<ContractCodeNode> children) {
        for(File file : files) {
            ContractCodeNode node = new ContractCodeNode();
            children.add(node);
            node.setName(extractFileName(file));
            node.setPath(extractFilePath(file));
            node.setDir(file.isDirectory());
            if(file.isDirectory()) {
                node.setChildren(new ArrayList<>());
                recursive(file.listFiles(), node.getChildren());
            }
        }
    }

    private String extractFileName(File file) {
        if(file.isDirectory()) {
            return file.getName();
        }
        String name = file.getName();
        name = name.replaceAll("\\.java", "");
        return name;
    }

    private String extractFilePath(File file) {
        String path = file.getPath();
        path = path.replaceAll(BASE, "");
        return path;
    }

    @RpcMethod("getContractCode")
    public RpcResult getContractCode(List<Object> params) {
        RpcResult result = new RpcResult();
        FileInputStream in = null;
        try {
            VerifyUtils.verifyParams(params, 2);
            String contractAddress = (String) params.get(0);
            if (!AddressTool.validAddress(contractAddress)) {
                result.setError(new RpcResultError(RpcErrorCode.PARAMS_ERROR, "[contractAddress] is inValid"));
                return result;
            }
            //TODO 检查认证状态，通过认证的合约继续下一步

            String filePath = (String) params.get(1);
            File file = new File(filePath);
            in = new FileInputStream(file);
            List<String> strings = IOUtils.readLines(in);
            StringBuilder sb = new StringBuilder();
            strings.forEach(a -> {
                sb.append(a).append("\r\n");
            });
            result.setResult(sb.toString());
        } catch (FileNotFoundException e) {
            Log.error(e);
            result.setError(new RpcResultError(RpcErrorCode.PARAMS_ERROR, e.getMessage()));
        } catch (IOException e) {
            Log.error(e);
            result.setError(new RpcResultError(RpcErrorCode.PARAMS_ERROR, e.getMessage()));
        } finally {
            IOUtils.closeQuietly(in);
        }
        return result;
    }
}
