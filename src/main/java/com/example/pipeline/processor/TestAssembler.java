import com.example.pipeline.processor.AssemblerEngine;
import com.example.pipeline.model.Error;
import java.util.ArrayList;
import java.util.List;

public class TestAssembler {
    public static void main(String[] args) {
        String code = "START 100\n" +
                      "MOVER AREG, ='5'\n" +
                      "ADD AREG, ='3'\n" +
                      "MOVEM AREG, X\n" +
                      "X DS 1\n" +
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
    }
}
