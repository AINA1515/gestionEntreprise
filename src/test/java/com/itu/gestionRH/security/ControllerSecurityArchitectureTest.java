package com.itu.gestionRH.security;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

class ControllerSecurityArchitectureTest {
    @Test
    void everyMappedControllerMethodIsProtectedUnlessExplicitlyPublic() throws Exception {
        var resolver = new PathMatchingResourcePatternResolver();
        var resources = resolver.getResources("classpath*:com/itu/gestionRH/controller/**/*.class");
        MetadataReaderFactory readers = new CachingMetadataReaderFactory(resolver);
        List<String> violations = new ArrayList<>();
        for (var resource : resources) {
            var reader=readers.getMetadataReader(resource);
            if (!reader.getAnnotationMetadata().hasAnnotation(RestController.class.getName())) continue;
            Class<?> type=ClassUtils.forName(reader.getClassMetadata().getClassName(), getClass().getClassLoader());
            boolean classSecured=AnnotatedElementUtils.hasAnnotation(type, PreAuthorize.class);
            String base=join(AnnotatedElementUtils.findMergedAnnotation(type, RequestMapping.class));
            for(Method method:type.getDeclaredMethods()) {
                RequestMapping mapping=AnnotatedElementUtils.findMergedAnnotation(method,RequestMapping.class);
                if(mapping==null) continue;
                String path=base+join(mapping);
                String verb=mapping.method().length==0?"":mapping.method()[0].name();
                boolean isPublic=(verb.equals("GET")&&path.equals("/api/v1/ping"))
                        ||(verb.equals("POST")&&(path.equals("/api/v1/auth/login")||path.equals("/api/v1/auth/inscription-candidat")))
                        ||(verb.equals("GET")&&path.equals("/api/v1/recrutement/offres"));
                if(!isPublic&&!classSecured&&!AnnotatedElementUtils.hasAnnotation(method,PreAuthorize.class)) violations.add(type.getSimpleName()+"#"+method.getName()+" "+verb+" "+path);
            }
        }
        assertTrue(violations.isEmpty(),"Endpoints sans @PreAuthorize : "+violations);
    }
    private static String join(RequestMapping mapping){
        if(mapping==null||mapping.path().length==0)return "";
        return mapping.path()[0].startsWith("/")?mapping.path()[0]:"/"+mapping.path()[0];
    }
}
