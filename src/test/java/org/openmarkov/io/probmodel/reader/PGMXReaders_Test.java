package org.openmarkov.io.probmodel.reader;

import org.jdom2.Element;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openmarkov.core.io.ProbNetReader;
import org.openmarkov.core.io.format.annotation.FormatType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.java.classUtils.ClassUtils;
import org.openmarkov.plugin.PluginSearch;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Stream;

class PGMXReaders_Test {
    
    static final HashSet<Class<? extends ProbNetReader>> READERS_THAT_CAN_MISS_POTENTIAL_READER_METHODS
            = new HashSet<>(List.of(PGMXReader_0_2.class));
    
    record TestData(PGMXReader_0_2 pgmxReader, Class<? extends Potential> potentialClass,
                    boolean requiresToReadAllPotentials) {
    }
    
    @ParameterizedTest
    @MethodSource("generateTestData")
    public void testAllReaders(TestData testData) {
        Method method = testData.pgmxReader.potentialGenerators.get(testData.potentialClass);
        if (method == null) {
            if (testData.requiresToReadAllPotentials) {
                Assertions.fail("No potential reader method found in " + testData.getClass() + " for potential of class " + testData.potentialClass);
            }
            return;
        }
        var params = method.getParameterTypes();
        Assertions.assertArrayEquals(new Class[]{Element.class, ProbNet.class, PotentialRole.class, List.class}, params,
                                     "Parameters of reader method " + method + " should be (Element xmlPotential, ProbNet probNet, PotentialRole xmlRole, List<Variable> variables)");
        Assertions.assertTrue(Potential.class.isAssignableFrom(method.getReturnType()), method + " should return a Potential");
        Assertions.assertTrue(method.getReturnType()
                                    .isAssignableFrom(testData.potentialClass), method + " returns a " + method.getReturnType() + ", which cannot be casted into " + testData.potentialClass);
    }
    
    public static Stream<TestData> generateTestData() {
        var readers = PluginSearch.init()
                                  .extending(PGMXReader_0_2.class)
                                  .annotatedWith(FormatType.class)
                                  .filter(ClassUtils::isConcrete)
                                  .stream()
                                  //.filter(reader -> reader
                                  //        .getAnnotation(FormatType.class).extension().equalsIgnoreCase("PGMX"))
                                  .sorted(Comparator.comparing(reader -> reader
                                          .getAnnotation(FormatType.class).version()))
                                  .toList();
        
        return readers.stream().flatMap(readerClass -> {
            PGMXReader_0_2 reader;
            try {
                reader = readerClass.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | NoSuchMethodException | IllegalAccessException |
                     InvocationTargetException e) {
                throw new RuntimeException(e);
            }
            boolean requiresToReadAllPotentials = !PGMXReaders_Test.READERS_THAT_CAN_MISS_POTENTIAL_READER_METHODS.contains(readerClass);
            return PluginSearch.init()
                               .extending(Potential.class)
                               .filter(ClassUtils::isConcrete)
                               .stream()
                               .map(potentialClass -> new TestData(reader, potentialClass, requiresToReadAllPotentials));
        });
    }
    
    
}