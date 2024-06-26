package me.ionar.salhack.module;

public class Value<T> {

    public ValueListeners Listener;
    private String name;
    private String[] alias;
    private String desc;
    private Module Mod;
    private T value;

    private T min;
    private T max;
    private T inc;

    public Value(String name, String[] alias, String desc) {
        this.name = name;
        this.alias = alias;
        this.desc = desc;
    }

    public Value(String name, String[] alias, String desc, T value) {
        this(name, alias, desc);
        this.value = value;
    }

    public Value(String name, String[] alias, String desc, T value, T min, T max, T inc) {
        this(name, alias, desc, value);
        this.min = min;
        this.max = max;
        this.inc = inc;
    }

    public <T> T clamp(T value, T min, T max) {
        return ((Comparable) value).compareTo(min) < 0 ? min : (((Comparable) value).compareTo(max) > 0 ? max : value);
    }

    public T getValue() {
        return this.value;
    }

    public void setValue(T value) {
        if (min != null && max != null) {
            final Number val = (Number) value;
            final Number min = (Number) this.min;
            final Number max = (Number) this.max;
            this.value = (T) val;
            // this.value = (T) this.clamp(val, min, max);
        } else {
            this.value = value;
        }

        if (Mod != null)
            Mod.SignalValueChange(this);
        if (Listener != null)
            Listener.OnValueChange(this);
    }

    public String GetNextEnumValue(boolean reverse) {
        final Enum currEnum = (Enum) this.getValue();

        int i = 0;

        for (; i < this.value.getClass().getEnumConstants().length; i++) {
            final Enum e = (Enum) this.value.getClass().getEnumConstants()[i];
            if (e.name().equalsIgnoreCase(currEnum.name())) {
                break;
            }
        }

        return this.value.getClass()
                .getEnumConstants()[(reverse ? (i != 0 ? i - 1 : value.getClass().getEnumConstants().length - 1)
                : i + 1) % value.getClass().getEnumConstants().length].toString();
    }

    public int getEnum(String input) {
        for (int i = 0; i < this.value.getClass().getEnumConstants().length; i++) {
            final Enum e = (Enum) this.value.getClass().getEnumConstants()[i];
            if (e.name().equalsIgnoreCase(input)) {
                return i;
            }
        }
        return -1;
    }

    public Enum GetEnumReal(String input) {
        for (int i = 0; i < this.value.getClass().getEnumConstants().length; i++) {
            final Enum e = (Enum) this.value.getClass().getEnumConstants()[i];
            if (e.name().equalsIgnoreCase(input)) {
                return e;
            }
        }
        return null;
    }

    public void setEnumValue(String value) {
        for (Enum e : ((Enum) this.value).getClass().getEnumConstants()) {
            if (e.name().equalsIgnoreCase(value)) {
                setValue((T) e);
                break;
            }
        }

        if (Mod != null)
            Mod.SignalEnumChange();
    }

    public T getMin() {
        return min;
    }

    public void setMin(T min) {
        this.min = min;
    }

    public T getMax() {
        return max;
    }

    public void setMax(T max) {
        this.max = max;
    }

    public T getInc() {
        return inc;
    }

    public void setInc(T inc) {
        this.inc = inc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getAlias() {
        return alias;
    }

    public void setAlias(String[] alias) {
        this.alias = alias;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void SetListener(ValueListeners vListener) {
        Listener = vListener;
    }

    public void InitalizeMod(Module mod1) {
        Mod = mod1;
    }

    public void SetForcedValue(T value) {
        if (min != null && max != null) {
            final Number val = (Number) value;
            final Number min = (Number) this.min;
            final Number max = (Number) this.max;
            this.value = (T) val;
            // this.value = (T) this.clamp(val, min, max);
        } else {
            this.value = value;
        }
    }
}
