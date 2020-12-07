import net.minecraft.block.BlockState;

@Deprecated
public class RedstoneCircuitDiagramRF {
    public RedstoneCircuitDiagramRF(BlockState[][][] area) {
        SimpleScanner simpleScanner = new SimpleScanner(area);
        simpleScanner.statusReport();
    }
}
