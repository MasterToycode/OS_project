



public class Instruction {
    public int id;//指令的ID
    public int state;//指令的状态
    public Instruction(int id, int state) {
        this.id = id;
        this.state = state;
    }

    public int getState() {
        return state;
    }

    @Override
    public String toString() {
        return "Instruction{" +
                "id=" + id +
                ", state=" + state +
                '}';
    }
}
