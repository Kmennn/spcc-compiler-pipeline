package com.example.pipeline.processor;
import java.util.ArrayList;
import java.util.List;
import com.example.pipeline.model.Error;

public class TestAssembler2 {
    public static void main(String[] args) {
        String code = "START 200\n" +
                      "MOVER AREG, =\"5\"\n" +
                      "ADD AREG, BREG\n" +
                      "MOVER CREG, =\"3\"\n" +
                      "LTORG\n" +
                      "END";
        
        AssemblerEngine engine = new AssemblerEngine();
        List<Error> errors = new ArrayList<>();
        AssemblerEngine.AssemblerResult result = engine.assemble(code, errors);
        
        System.out.println("--- INTERMEDIATE CODE ---");
        for (AssemblerEngine.ICLine line : result.intermediateCode) {
            System.out.println(line.lc + "\t" + line.ic);
        }
        
        System.out.println("\n--- MACHINE CODE ---");
        for (String mc : result.machineCode) {
            System.out.println(mc);
        }
        
        System.out.println("\n--- LITTAB ---");
        for (AssemblerEngine.Literal lit : result.littab) {
            System.out.println(lit.value + "\t" + lit.address);
        }
        System.out.println("\n--- ERRORS ---");
        for (Error e : errors) {
            System.out.println(e.line + ": " + e.message);
        }
    }
}
