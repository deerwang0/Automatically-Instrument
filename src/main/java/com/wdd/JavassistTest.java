package com.wdd;

import com.wdd.entities.Person;
import javassist.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.lang.reflect.Method;
import java.util.*;

import static com.wdd.utils.ClassUtil.getMethodSet;
import static com.wdd.utils.ClassUtil.getStartAndEndOfMethod;

//@Slf4j
public class JavassistTest {


    public static void main(String[] args) throws Exception {
        Person person = new Person();
        person.getId();person.getName();person.getNickName();
        person.setId();person.setName();person.setNickName();

        List<Integer> linesToInsertList = new ArrayList<Integer>();
        linesToInsertList.add(1);
        linesToInsertList.add(32);


        insertLogicIntoFile(linesToInsertList);
    }


    private static void insertLogicIntoFile(List<Integer> linesToInsertList) throws Exception {

        String dirPath = "C:\\Users\\13572\\Desktop\\demo\\src\\main\\java\\com\\wdd\\entities";
        String appName = "demo";
        if (StringUtils.isBlank(dirPath)) {
//            log.error("文件夹不存在");
            return;
        }

        File dir = new File(dirPath);

        if (!dir.exists() || !dir.isDirectory()) {
//            log.error("文件夹不存在");
            return;
        }

        File[] fileArray = dir.listFiles();
        if (ArrayUtils.isEmpty(fileArray)) {
//            log.error("文件夹下没有.java文件");
        }

        for (File file : fileArray) {

            String absolutePath = file.getAbsolutePath()
                    .replaceAll("/", ".")
                    .replaceAll("\\\\",".")
                    .replace(".java", "");

            if (absolutePath.indexOf(appName) != -1) {
                absolutePath = absolutePath.substring(absolutePath.indexOf(appName) + appName.length() + 10);
            }

            Set<String> methodSet = getMethodSet(Class.forName(absolutePath));

            Map<String, List<Integer>> lineIndexsCouldInsertLogicByMethodMap = getStartAndEndOfMethod(file.getAbsolutePath(), methodSet);


            Map<String/*lineIndex*/, String/*methodName*/> methodByLineIndexMap = new HashMap<String, String>();
            for (Map.Entry<String, List<Integer>> entry : lineIndexsCouldInsertLogicByMethodMap.entrySet()) {
                String methodName = entry.getKey();
                List<Integer> lineIndexList = entry.getValue();
                for (Integer lineIndex : lineIndexList) {
                    methodByLineIndexMap.put(lineIndex.toString(), methodName);
                }
            }


            insertLogic(Class.forName(absolutePath), methodByLineIndexMap, linesToInsertList);
        }
    }

    private static void insertLogic(Class clazz, Map<String, String> lineIndexByMethodMap, List<Integer> linesToInsertList) throws Exception {
        String logicToInsert = "System.out.println(\"我是运行时被插入到源文件第{index}行的\");";

        ClassPool pool = ClassPool.getDefault();
        pool.insertClassPath(new ClassClassPath(clazz));

        CtClass ctClass = pool.get(clazz.getName());

        for (Integer lineIndex : linesToInsertList) {
            if (lineIndexByMethodMap.containsKey(lineIndex.toString())) {
                CtMethod ctMethod = ctClass.getDeclaredMethod(lineIndexByMethodMap.get(lineIndex.toString()));
                ctMethod.insertAt(lineIndex.intValue(), "System.out.println(\"被插入到第" + lineIndex + "行\");");
            }else {
                System.out.println("行号不正确:" + lineIndex);
            }
        }
        ctClass.writeFile("C:\\Users\\13572\\Desktop\\demo\\target\\classes");


    }
}
