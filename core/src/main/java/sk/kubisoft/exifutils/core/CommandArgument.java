package sk.kubisoft.exifutils.core;

/**
 * Represents a command-line argument (not option) specification
 */
public class CommandArgument {

    private final String name;
    private final boolean required;
    private final boolean multiple;

    private CommandArgument(Builder builder) {
        this.name = builder.name;
        this.required = builder.required;
        this.multiple = builder.multiple;
    }

    public static class Builder {
        private final String name;
        private boolean required = true;
        private boolean multiple = false;

        public Builder(String name) {
            this.name = name;
        }

        public Builder optional() {
            this.required = false;
            return this;
        }

        public Builder multiple() {
            this.multiple = true;
            return this;
        }

        public CommandArgument build() {
            return new CommandArgument(this);
        }
    }

    public String getName() {
        return name;
    }

    public boolean isRequired() {
        return required;
    }

    public boolean isMultiple() {
        return multiple;
    }

}
