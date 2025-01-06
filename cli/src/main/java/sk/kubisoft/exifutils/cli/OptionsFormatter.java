package sk.kubisoft.exifutils.cli;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import sk.kubisoft.exifutils.core.CommandArgument;

import java.util.ArrayList;
import java.util.List;

public class OptionsFormatter {

    public static String generateUsageSyntax(Options options, List<CommandArgument> arguments) {
        StringBuilder syntax = new StringBuilder();
        List<Option> optionalOpts = new ArrayList<>();
        List<Option> requiredOpts = new ArrayList<>();

        // Separate required and optional options
        for (Option opt : options.getOptions()) {
            if (opt.isRequired()) {
                requiredOpts.add(opt);
            } else {
                optionalOpts.add(opt);
            }
        }

        // Add optional options first
        for (Option opt : optionalOpts) {
            syntax.append("[");
            appendOptionSyntax(syntax, opt);
            syntax.append("] ");
        }

        // Add required options
        for (Option opt : requiredOpts) {
            appendOptionSyntax(syntax, opt);
            syntax.append(" ");
        }

        // Add arguments according to their specification
        if (arguments != null) {
            for (CommandArgument arg : arguments) {
                if (!arg.isRequired()) {
                    syntax.append("[");
                }

                syntax.append(arg.getName());

                if (arg.isMultiple()) {
                    syntax.append("...");
                }

                if (!arg.isRequired()) {
                    syntax.append("]");
                }

                syntax.append(" ");
            }
        }

        return syntax.toString().trim();
    }

    private static void appendOptionSyntax(StringBuilder syntax, Option opt) {
        syntax.append("-").append(opt.getOpt());
        if (opt.hasArg()) {
            syntax.append(" ");
            if (opt.getArgName() != null) {
                syntax.append(opt.getArgName());
            } else {
                syntax.append("arg");
            }
        }
    }
}
