package com.code_inspector.plugins.intellij.dependencies;

import com.code_inspector.plugins.intellij.model.Dependency;
import com.google.common.collect.ImmutableList;
import com.google.gson.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.VisibleForTesting;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.code_inspector.plugins.intellij.Constants.*;

public class PythonDependency extends AbstractDependency{

    public static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);

    @VisibleForTesting
    public static List<Dependency> getDependenciesFromInputStream(InputStream inputStream) {
        List<Dependency> result = new ArrayList<>();
        String pattern = "^([a-zA-Z0-9\\-]+)";
        Pattern r = Pattern.compile(pattern);
        try{
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            while ((line = bufferedReader.readLine()) != null){
                Matcher m = r.matcher(line);
                if (m.find( )) {
                    result.add(new Dependency(m.group(0), Optional.empty()));
                }
            }

            return result;
        }
        catch (IOException e) {
            LOGGER.info("PythonDependency - getDependenciesFromInputStream - error when parsing the file");
            return ImmutableList.of();
        }
    }

    @Override
    public List<Dependency> getDependencies(PsiFile psiFile) {
        Optional<VirtualFile> dependencyFile = this.getDependencyFile(psiFile, PYTHON_DEPENDENCY_FILE);
        if(!dependencyFile.isPresent()) {
            return ImmutableList.of();
        }

        try {
            InputStream inputStream = dependencyFile.get().getInputStream();
            List<Dependency> result = getDependenciesFromInputStream(inputStream);
            inputStream.close();
            return result;
        } catch (IOException e){
            LOGGER.error("PythonDependency - getDependenciesFromInputStream - error when opening the file");
            return ImmutableList.of();
        }
    }
}
