public class Memory_Block {
    private boolean Occupied;//是否被占用
    private int pcb_id;//占用该进程的ID
    public static int BLOCK_SIZE=1000;//每个内存块的大小为：1000B

    public int getBlock_ID() {
        return Block_ID;
    }

    public void setBlock_ID(int block_ID) {
        Block_ID = block_ID;
    }

    public int Block_ID;//每一个物理块的id
    public int getoccupied() {
        return occupied;
    }
    public void setoccupied(int occupied) {
        this.occupied = occupied;
    }
    private int occupied;//占用该物理块的进程实际占用了多少空间
    public  void setBlockSize(int blockSize) {
        BLOCK_SIZE = blockSize;
    }
    public boolean isOccupied() {
        return Occupied;
    }
    public void setOccupied(boolean occupied) {
        Occupied = occupied;
    }
    public int getPcb_id() {
        return pcb_id;
    }
    public void setPcb_id(int pcb_id) {
        this.pcb_id = pcb_id;
    }
    public Memory_Block() {
    }


    public Memory_Block(boolean occupied, int pcb_id, int block_ID) {
        this.Occupied = occupied;
        this.pcb_id = pcb_id;
        this.Block_ID = block_ID;
    }
}
