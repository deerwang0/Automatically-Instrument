package com.wdd.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @description: class工具类
 */
@Slf4j
public final class ClassUtil {

    private static String[] keyWordArray = new String[]{"continue","break","return"};


    /**
     * 获取某个类的所有方法。（方法重载要规避
     * @param clazz
     * @return
     * @throws Exception
     */
    public static Set<String> getMethodSet(Class clazz) throws Exception {
        Set<String> methodSet = new HashSet<String>();

        if (null == clazz) {
            log.error("传入class为null");
            return methodSet;
        }

        Method[] methods = clazz.getDeclaredMethods();

        if (ArrayUtils.isEmpty(methods)) {
            log.error("该类没有方法");
            return methodSet;
        }

        for (Method method : methods) {
            methodSet.add(method.getName());
        }

        return methodSet;
    }


    /**
     * 读取Java源文件，获取每个方法的可以往某一行前插逻辑的行数。（内部类要规避，代码要规范。。。
     * @param filePath
     * @param methodSet
     * @return
     */
    public static Map<String, List<Integer>> getStartAndEndOfMethod(String filePath, Set<String> methodSet) throws Exception {
        Map<String, List<Integer>> startAndEndOfMethodMap = new HashMap<String, List<Integer>>();

        if (StringUtils.isBlank(filePath) || !filePath.toUpperCase().endsWith(".JAVA")) {
            log.error("传入文件必须为Java文件");
            return startAndEndOfMethodMap;
        }

        File sourceFile = new File(filePath);

        if (!sourceFile.exists() || !sourceFile.isFile()) {
            log.error("文件不存在或传入了文件夹");
            return startAndEndOfMethodMap;
        }

        Reader reader = new FileReader(filePath);
        BufferedReader bufferedReader = new BufferedReader(reader);


        startAndEndOfMethodMap = handleFileContent(bufferedReader, methodSet);

        bufferedReader.close();
        reader.close();

        return startAndEndOfMethodMap;
    }

    /**
     * 读取文件内容
     * @param bufferedReader
     * @param methodSet
     * @return
     * @throws IOException
     */
    private static Map<String, List<Integer>> handleFileContent(BufferedReader bufferedReader, Set<String> methodSet) throws IOException {
        Map<String, List<Integer>> startAndEndOfMethodMap = new HashMap<String, List<Integer>>();

        //表示当前是否在方法里边
        boolean inMethodFlag = false;
        //记录在方法中尚未配对的大括号数量
        int bracketIndex = 0;
        //记录正在处理的方法名
        String methodNameProcessing = null;
        //记录当前方法可以插入的行
        List<Integer> lineIndexList = null;
        //标识当前行是否在注释中
        boolean commentFlag = false;
        //记录是否遇到了java关键字（continue,break,return），如果遇到java关键字，需要匹配一次大括号才可以继续插入逻辑
        boolean keyWordFlag = false;

        //记录上一行是否是完整的句子。
        boolean finishedLastLineFlag = true;

        int lineIndex = 1;
        String line = bufferedReader.readLine();
        while (line != null) {

            if (StringUtils.isBlank(line)) {
                //空行直接过直接过
                line = bufferedReader.readLine();
                lineIndex++;
                continue;
            }

            //先处理注释
            if (line.trim().startsWith("//")) {
                //单行注释直接过
                line = bufferedReader.readLine();
                lineIndex++;
                continue;
            } else if (!commentFlag && line.trim().startsWith("/*")) {
                /**
                 * 我是注释，注释开始行
                 */
                commentFlag = true;

                line = bufferedReader.readLine();
                lineIndex++;
                continue;

            } else if (commentFlag) {
                if (line.indexOf("*/") != -1) {

                    /**
                     * 我是注释，注释结束行
                     */
                    commentFlag = false;
                }

                line = bufferedReader.readLine();
                lineIndex++;
                continue;
            }


            if (inMethodFlag) {


                if (!keyWordFlag && bracketIndex > 0 && finishedLastLineFlag) {
                    lineIndexList.add(lineIndex);
                    startAndEndOfMethodMap.put(methodNameProcessing, lineIndexList);
                }


                if (line.trim().endsWith("{")) {
                    bracketIndex++;
                }

                if (line.trim().startsWith("}")) {
                    keyWordFlag = false;
                    bracketIndex--;

                    //大括号全部匹配完，表示方法结束
                    if (bracketIndex == 0) {
                        inMethodFlag = false;
                    }
                }

                //下边的if-elseif及for循环是为下一行准备，判断语义是否结束，是否包含关键字
                if ((line.indexOf(";") != -1) || (line.indexOf("{") != -1) || (line.indexOf("}") != -1)) {
                    finishedLastLineFlag = true;
                } else if (finishedLastLineFlag && StringUtils.isBlank(line)){
                    finishedLastLineFlag = true;
                } else {
                    finishedLastLineFlag = false;
                }

                for (String keyWord : keyWordArray) {
                    if (line.indexOf(keyWord) != -1) {
                        keyWordFlag = true;
                        break;
                    }
                }

            } else if (StringUtils.isNotBlank(line)){

                for (String method : methodSet) {
                    if (line.trim().indexOf(method) != -1) {
                        inMethodFlag = true;
                        bracketIndex = 0;
                        lineIndexList = new ArrayList<Integer>();
                        methodNameProcessing = new String(method);

                        break;
                    }
                }
                if (inMethodFlag) {
                    continue;
                }

            }



            line = bufferedReader.readLine();
            lineIndex++;
        }

        return startAndEndOfMethodMap;
    }



}
