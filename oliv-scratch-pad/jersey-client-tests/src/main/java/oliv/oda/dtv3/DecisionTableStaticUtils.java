package oliv.oda.dtv3;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DecisionTableStaticUtils {

    private static boolean verbose = true;

    enum QueryOption {
        QUERY,
        BAG_ENTITY
    }

    private static ObjectMapper mapper = new ObjectMapper();

    private static List<String> getInputItemList(Map<String, Object> decisionMap) {
        List<String> inputParams = new ArrayList<>();
        // Find required input data
//        Object requiredInputData = decisionMap.get("requiredInputData"); // Also available in logic.references
//        Object items = ((Map)requiredInputData).get("items");
//        List<Map<String, Object>> itemList = (List)items;
//        itemList.forEach(item -> {
//            String value = (String)((Map)item).get("name");
//            inputParams.add(value);
//        });

        List<Map<String, Object>> itemList = (List)((Map)decisionMap.get("logic")).get("inputs");
        itemList.forEach(item -> {
            String value = (String)((Map)item.get("inputExpression")).get("value");
            inputParams.add(value);
        });

        return inputParams;
    }

    private static List<String> getOutputItemList(Map<String, Object> decisionMap) {
        List<String> outputParams = new ArrayList<>();
        List<Map<String, Object>> itemList = (List)((Map)decisionMap.get("logic")).get("outputs");
        itemList.forEach(item -> {
            String value = (String)((Map)item).get("name");
            outputParams.add(value);
        });

        return outputParams;
    }

    private static String stripQuotes(String in) {
        String out = in;
        if (out != null) {
            out = out.trim();
            if ((out.startsWith("\"") && out.endsWith("\"")) ||
                    (out.startsWith("'") && out.endsWith("'"))) {
                out = out.substring(1, out.length() - 1);
            }
        }
        return out;
    }

    private static DecisionContext setUpdateContext(Map<String, Object> decisionMap,
                                                    Map<String, Object> userCtx,
                                                    Map<String, Object> upsertStmt) throws Exception {
        DecisionContext context = new DecisionContext();
        Map<String, Object> upsert = (Map)upsertStmt.get("upsert");
        Map<String, Object> where = (Map)upsertStmt.get("where");

//        String decisionName = (String)decisionMap.get("name");
        List<String> inputItems = getInputItemList(decisionMap);
        List<String> outputItems = getOutputItemList(decisionMap);
        if (verbose) {
            System.out.println("--- INPUT ITEMS ---");
            inputItems.forEach(System.out::println);
            System.out.println("-------------------");
            System.out.println("-- OUTPUT ITEMS ---");
            outputItems.forEach(System.out::println);
            System.out.println("-------------------");
        }

        context.setInputItems(inputItems);
        context.setOutputItems(outputItems);

        List<Map<String, Object>> userAndWhere = Arrays.asList(userCtx, where);
        userAndWhere.forEach(map -> {
            if (map != null) {
                map.keySet().forEach(k -> {
                    String item = k;
                    String value = (map.get(k) == null ? null : map.get(k).toString());
                    context.addWhereColumnId(item);
                    context.addWhereColumnValue(value);
//                    System.out.printf("Adding to the WHERE clause: %s => %s\n", item, value);
                });
            }
        });
        context.getWhereColumnId().forEach(columnId -> {
            if (!inputItems.contains(columnId) && !outputItems.contains(columnId)) {
                throw new InvalidParameterException(String.format("WHERE [%s] Not Found in item list", columnId));
            }
            // Find indexes.
            DecisionContext.TargetColumnIndex idx = outputItems.contains(columnId) ?
                    new DecisionContext.TargetColumnIndex(outputItems.indexOf(columnId), DecisionContext.TargetColumnIndex.InOrOut.OUTPUT) :
                    new DecisionContext.TargetColumnIndex(inputItems.indexOf(columnId));
            context.addWhereColumnIndex(idx);
        });

        if (upsert != null) {
            upsert.keySet().forEach(k -> {
                String itemName = k;
                String updateTo = upsert.get(k).toString();
                context.addTargetColumnId(itemName);
                // Operation, on the value to update
                DecisionContext.Operation op = DecisionContext.detectOperation(updateTo);
                String extracted = null;
                if (op != null) { // Extract value
                    extracted = DecisionContext.extractFunctionParameter(updateTo, op);
                }
                context.addTargetColumnValue(extracted == null ? updateTo : stripQuotes(extracted));
                context.addOperation(op);
                context.addRawTargetColumnValue(updateTo);
                context.addTargetColumnIndex(outputItems.contains(itemName) ?
                        new DecisionContext.TargetColumnIndex(outputItems.indexOf(itemName), DecisionContext.TargetColumnIndex.InOrOut.OUTPUT) :
                        new DecisionContext.TargetColumnIndex(inputItems.indexOf(itemName)));

                if (!outputItems.contains(itemName) && !inputItems.contains(itemName)) {
                    throw new InvalidParameterException(String.format("%s [%s] Not Found in item list [%s, %s]", "UPDATE", itemName,
                            inputItems.stream().collect(Collectors.joining(", ")),
                            outputItems.stream().collect(Collectors.joining(", "))));
                }
            });
        }
        return context;
    }

    private static DecisionContext buildQueryDecisionContext(List<String> inputItems,
                                                             List<String> outputItems,
                                                             List<Object> query,
                                                             Map<String, Object> where,
                                                             Map<String, Object> userCtx) {
        DecisionContext context = new DecisionContext();

        context.setInputItems(inputItems);
        context.setOutputItems(outputItems);

        List<Map<String, Object>> userAndWhere = Arrays.asList(userCtx, where);
        userAndWhere.forEach(map -> {
            if (map != null) {
                map.keySet().forEach(k -> {
                    String item = k;
                    String value = (map.get(k) == null ? null : map.get(k).toString());
                    context.addWhereColumnId(item);
                    context.addWhereColumnValue(value);
//                    System.out.printf("Adding to the WHERE clause: %s => %s\n", item, value);
                });
            }
        });

        context.getWhereColumnId().forEach(columnId -> {
            if (!inputItems.contains(columnId) && !outputItems.contains(columnId)) {
                throw new InvalidParameterException(String.format("WHERE [%s] Not Found in item list", columnId));
            }
            // Find indexes.
            DecisionContext.TargetColumnIndex idx = outputItems.contains(columnId) ?
                    new DecisionContext.TargetColumnIndex(outputItems.indexOf(columnId), DecisionContext.TargetColumnIndex.InOrOut.OUTPUT) :
                    new DecisionContext.TargetColumnIndex(inputItems.indexOf(columnId));
            context.addWhereColumnIndex(idx);
        });

        if (query != null) {
            query.forEach(item -> {
                String itemName = (String)item;
                context.addTargetColumnId(itemName);
                DecisionContext.Operation op = DecisionContext.detectOperation(itemName);
                String extracted = null;
                if (op != null) { // Extract value
                    extracted = DecisionContext.extractFunctionParameter(itemName, op);
                }
                if (extracted != null) {
                    context.addTargetColumnId(extracted);
                }
                context.addOperation(op);
                context.addRawTargetColumnValue(itemName);

                String itemToUse = (extracted != null ? stripQuotes(extracted) : itemName);
                if (!outputItems.contains(itemToUse) && !inputItems.contains(itemToUse)) {
                    throw new InvalidParameterException(String.format("%s [%s] Not Found in item list", "QUERY", itemToUse));
                }
                context.addTargetColumnIndex(outputItems.contains(itemToUse) ?
                        new DecisionContext.TargetColumnIndex(outputItems.indexOf(itemToUse), DecisionContext.TargetColumnIndex.InOrOut.OUTPUT) :
                        new DecisionContext.TargetColumnIndex(inputItems.indexOf(itemToUse)));
            });
        }
        // Add where columns, if not there yet.
        if (context.getWhereColumnId() != null && context.getWhereColumnId().size() > 0) {
            context.getWhereColumnId().forEach(whereColumnId -> {
                if (!context.getTargetColumnId().contains(whereColumnId)) {
                    context.addTargetColumnId(whereColumnId);
                    context.addOperation(null); // TODO Check if that's right. There must be a better thing to do about operations...
                    context.addTargetColumnIndex(context.getWhereColumnIndex().get(context.getWhereColumnId().indexOf(whereColumnId)));
                }
            });
        }
        return context;
    }

    private static DecisionContext setQueryContext(Map<String, Object> decisionMap,
                                                   Map<String, Object> userCtx,
                                                   Map<String, Object> selectStmt) throws Exception {
        List<Object> query = (List)selectStmt.get("select");
        Map<String, Object> where = (Map)selectStmt.get("where");

        List<String> inputItems = getInputItemList(decisionMap);
        List<String> outputItems = getOutputItemList(decisionMap);
        if (verbose) {
            System.out.println("--- INPUT ITEMS ---");
            inputItems.forEach(System.out::println);
            System.out.println("-------------------");
            System.out.println("-- OUTPUT ITEMS ---");
            outputItems.forEach(System.out::println);
            System.out.println("-------------------");
        }
        return buildQueryDecisionContext(inputItems, outputItems, query, where, userCtx);
    }

    private static DecisionContext setBagEndContext(Map<String, Object> decisionMap,
                                                    Map<String, Object> userCtx,
                                                    Map<String, Object> bagEntity) throws Exception {
        List<String> inputItems = getInputItemList(decisionMap);
        List<String> outputItems = getOutputItemList(decisionMap);
        if (verbose) {
            System.out.println("--- INPUT ITEMS ---");
            inputItems.forEach(System.out::println);
            System.out.println("-------------------");
            System.out.println("-- OUTPUT ITEMS ---");
            outputItems.forEach(System.out::println);
            System.out.println("-------------------");
        }

        // query everything
        List<Object> query = new ArrayList<>();
        inputItems.forEach(query::add);
        outputItems.forEach(query::add);

        // Whenever items match
        Map<String, Object> where = new HashMap<>(); // (Map)bagEntity.get("where");
        bagEntity.keySet().forEach(k -> {
            if (inputItems.contains(k) || outputItems.contains(k)) {
                where.put(k, bagEntity.get(k));
            }
        });

        return buildQueryDecisionContext(inputItems, outputItems, query, where, userCtx);
    }

    private static List<Object> getValues(Map<String, Object> entryNode) {
        List<Object> values = new ArrayList<>();
        if (entryNode.get("value") != null) {
            values.add(entryNode.get("value"));
        } else if (entryNode.get("values") != null) {
            values.addAll((List)entryNode.get("values"));
        } else if (entryNode.get("range") != null) {
            values.add(((Map)entryNode.get("range")).get("endpoint1"));
        }
        return values;
    }

    private static void updateItemValue(Map<String, Object> item, String value) {
        updateItemValue(item, value, null);
    }
    private static void updateItemValue(Map<String, Object> item, String value, String rawValue) {
        if (verbose) {
            System.out.printf(">> Raw Value:%s\n", rawValue);
            try {
                System.out.printf("updateItemValue: %s\n", mapper.writeValueAsString(item));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        if (item.get("value") != null) {
            if ("-".equals((String)item.get("value")) && "Any".equals((String)item.get("mode"))) {
                List<String> allowedModes = (List)item.get("allowedModes");
                if (allowedModes != null && allowedModes.contains("Text")) {
                    item.put("mode", "Text");
                } else if (allowedModes != null && allowedModes.contains("Number")) {
                    item.put("mode", "Number");
                }
            }
            if ("Number".equals(item.get("mode")) && rawValue != null && rawValue.startsWith("range(")) { // TODO Refine that!!
                Map<String, Object> range = new HashMap<>(); // Map.of only after Java 9+
                range.put("operation1", "<"); // TODO Fix that hard-coded one!
                range.put("endpoint1", value);
                item.remove("value");
                item.put("range", range);
            } else {
                item.put("value", value);
            }
        } else if (item.get("values") != null) {
            List<Object> values = new ArrayList<>(); // New empty list
            values.add(value);
            item.put("values", values);
        } else if (item.get("range") != null) { // TODO Check if allowedModes contains Number ?
            ((Map)item.get("range")).put("endpoint1", value);
        } else {
            System.out.println("Watzat?");
        } // TODO Any
    }

    static String processUpdate(InputStream original, String userContext, String txSyntax) throws Exception {
        return processUpdate(original, userContext, txSyntax, null);
    }
    static String processUpdate(InputStream original, String userContext, String txSyntax, Map<String, Object> upsertResponseMap) throws Exception {
        // THE Decision Table Object
        Map<String, Object> jsonMap = mapper.readValue(original, Map.class);
        // The User Context
        Map<String, Object> user = mapper.readValue(userContext, Map.class);
        // The transformation directive
        Map<String, Object> tx = mapper.readValue(txSyntax, Map.class);
        // Set Update Context
        DecisionContext decisionUpdateContext = setUpdateContext(jsonMap, user, tx);

        final List<String> columnId = decisionUpdateContext.getWhereColumnId();
        final List<String> whereColumnValue = decisionUpdateContext.getWhereColumnValue();

        final List<String> targetColumnId = decisionUpdateContext.getTargetColumnId();
        final List<String> targetColumnNewValue = decisionUpdateContext.getTargetColumnValue();

        final List<DecisionContext.TargetColumnIndex> columnIndex = decisionUpdateContext.getWhereColumnIndex();
        final List<DecisionContext.TargetColumnIndex> targetColumnIndex = decisionUpdateContext.getTargetColumnIndex();

//        if (columnId != null) { // Mean there IS a where
//            System.out.printf("[%s] found at index %d\n", columnId, columnIndex);
//        }
//        System.out.printf("[%s] found at index %d\n", targetColumnId, targetColumnIndex);

        final List<Object> queryResult = new ArrayList<>();

        // Find the rules (line in the decision table)
        List<Map<String, Object>> rules = (List)((Map)jsonMap.get("logic")).get("rules");
        rules.forEach(rule -> {
            List<Map<String, Object>> inputEntries = (List)rule.get("inputEntries");
            List<Map<String, Object>> outputEntries = (List)rule.get("outputEntries");
            List<Map<String, Object>> annotationEntries = (List)rule.get("annotationEntries");

            if (verbose) {
                System.out.println("-- Row " + rules.indexOf(rule) + " --");
            }
            boolean conditionMet = true;
            for (int ii=0; ii<columnIndex.size(); ii++) {
                DecisionContext.TargetColumnIndex colIdx = columnIndex.get(ii);
                List<Object> values = (colIdx.getWhere() == DecisionContext.TargetColumnIndex.InOrOut.OUTPUT ?
                        Arrays.asList((String)outputEntries.get(colIdx.getIndex()).get("value")) :
                        getValues(inputEntries.get(colIdx.getIndex())));
                if (verbose) {
                    System.out.println("- Comparing column " +
                            "(" + colIdx.getIndex() + " " + colIdx.getWhere() + ") " +
                            decisionUpdateContext.getWhereColumnId().get(ii) +
                            ", val " + values +
                            " and " + whereColumnValue.get(ii));
                }
                boolean met = (whereColumnValue.get(ii) == null || values.contains(whereColumnValue.get(ii)));
                conditionMet = conditionMet && met;
            }

            if (conditionMet) {
                // Loop on the output columns
                for (int outIndex = 0; outIndex < decisionUpdateContext.targetColumnIndex.size(); outIndex++) {
                    Map<String, Object> oneRowResult = new HashMap<>();
                    oneRowResult.put("item", decisionUpdateContext.targetColumnId.get(outIndex));
                    if (decisionUpdateContext.getOperation().get(outIndex) != null) {
                        if (decisionUpdateContext.getOperation().get(outIndex).equals(DecisionContext.Operation.RANGE)) {
                            Map<String, Object> range = (Map) inputEntries.get(targetColumnIndex.get(outIndex).getIndex()).get("range");
                            if (range != null) {
                                Object endpoint1 = range.get("endpoint1");
                                if (verbose) {
                                    System.out.printf(">> Value for [%s]: currently [%s], moving to [%s]\n",
                                            (targetColumnId.get(outIndex) != null ? targetColumnId.get(outIndex) : "this line"),
                                            endpoint1,
                                            targetColumnNewValue.get(outIndex));
                                }
                                range.put("endpoint1", targetColumnNewValue.get(outIndex));
                                oneRowResult.put("from", endpoint1);
                                oneRowResult.put("to", targetColumnNewValue.get(outIndex));
                            } else {
                                oneRowResult.put("from", null);
                                oneRowResult.put("to", null);
                                System.out.println("No 'range' found where expected");
                                // throw new RuntimeException("No 'range' found where expected");
                            }
                        } else if (decisionUpdateContext.getOperation().get(outIndex).equals(DecisionContext.Operation.APPEND_TO_LIST)) {
                            List<String> values = (List) inputEntries.get(targetColumnIndex.get(outIndex).getIndex()).get("values");
                            if (values == null) {
                                String oneValue = (String) inputEntries.get(targetColumnIndex.get(outIndex).getIndex()).get("value");
                                if (oneValue != null) {
                                    values = new ArrayList();
                                    values.add(oneValue);
                                    inputEntries.get(targetColumnIndex.get(outIndex).getIndex()).put("values", values);
                                    inputEntries.get(targetColumnIndex.get(outIndex).getIndex()).remove("value");
                                }
                            }
                            if (values != null) {
                                // Check value validity
                                List<Map<String, Object>> suggestions = (List) inputEntries.get(targetColumnIndex.get(outIndex).getIndex()).get("suggestions");
                                AtomicBoolean found = new AtomicBoolean(false);
                                suggestions.forEach(suggestion -> {
                                    if (targetColumnNewValue.equals(suggestion.get("value"))) {
                                        found.set(true);
                                    }
                                });
                                if (found.get()) {
                                    AtomicBoolean alreadyThere = new AtomicBoolean(false);
                                    values.forEach(val -> {
                                        if (targetColumnNewValue.equals(val)) {
                                            alreadyThere.set(true);
                                        }
                                    });
                                    if (!alreadyThere.get()) {
                                        values.add(targetColumnNewValue.get(outIndex));
                                    } else {
                                        throw new RuntimeException(String.format("[%s] already there.", targetColumnNewValue));
                                    }
                                } else {
                                    throw new RuntimeException(String.format("[%s] not a valid option.", targetColumnNewValue));
                                }
                            } else {
                                throw new RuntimeException("No 'values' or 'value' found where expected");
                            }
                        } else if (decisionUpdateContext.getOperation().get(outIndex).equals(DecisionContext.Operation.DELETE_FROM_LIST)) {
                            // Leave at least one element...
                            List<String> values = (List) inputEntries.get(targetColumnIndex.get(outIndex).getIndex()).get("values");
                            if (values == null) {
                                String oneValue = (String) inputEntries.get(targetColumnIndex.get(outIndex).getIndex()).get("value");
                                if (oneValue != null) {
                                    if (targetColumnNewValue.get(outIndex).equals(oneValue)) {
                                        throw new RuntimeException(String.format("[%s] is the last value in the list. Leaving it.", targetColumnNewValue.get(outIndex)));
                                    } else {
                                        throw new RuntimeException(String.format("[%s] is not in the list", targetColumnNewValue.get(outIndex)));
                                    }
                                }
                            }
                            if (values != null) {
                                if (values.contains(targetColumnNewValue.get(outIndex))) {
                                    values.remove(targetColumnNewValue.get(outIndex));
                                } else {
                                    throw new RuntimeException(String.format("[%s] is not in the list", targetColumnNewValue.get(outIndex)));
                                }
                            } else {
                                throw new RuntimeException("No 'values' or 'value' found where expected");
                            }
                        }
                    } else {
                        // TODO Create a value node?
                        if ((targetColumnIndex.get(outIndex)).getWhere() != DecisionContext.TargetColumnIndex.InOrOut.OUTPUT) {
                            int index = (targetColumnIndex.get(outIndex)).getIndex();
                            Map<String, Object> targetObjectMap = inputEntries.get(index);
                            if (targetObjectMap.get("range") != null) { // TODO Check Mode ?
                                System.out.println(targetColumnNewValue.get(outIndex));
                                Map<String, Object> range = (Map)targetObjectMap.get("range");
                                range.put("endpoint1", targetColumnNewValue.get(outIndex));
                            } else if (targetObjectMap.get("value") != null) {
                                if ("-".equals((String)targetObjectMap.get("value")) && "Any".equals((String)targetObjectMap.get("mode"))) {
                                    List<String> allowedModes = (List)targetObjectMap.get("allowedModes");
                                    if (allowedModes != null && allowedModes.contains("Text")) {
                                        targetObjectMap.put("mode", "Text");
                                    } else if (allowedModes != null && allowedModes.contains("Number")) {
                                        targetObjectMap.put("mode", "Number");
                                    }
                                }
                                if ("Number".equals(targetObjectMap.get("mode")) ) { // && rawValue != null && rawValue.startsWith("range(")) { // TODO Refine that!!
                                    Map<String, Object> range = new HashMap<>(); // Map.of only after Java 9+
                                    range.put("operation1", "<"); // TODO Fix that hard-coded one!
                                    range.put("endpoint1", targetColumnNewValue.get(outIndex));
                                    targetObjectMap.remove("value");
                                    targetObjectMap.put("range", range);
                                } else {
                                    targetObjectMap.put("value", targetColumnNewValue.get(outIndex));
                                }
                            } else if (targetObjectMap.get("values") != null) {
                                // TODO Do it!! Manage values too!
                            } else {
                                throw new RuntimeException(String.format("Unmanaged operation [%s]", decisionUpdateContext.getRawTargetColumnValue().get(outIndex)));
                            }
                        } else {
                            String oldValue = (String) outputEntries.get(targetColumnIndex.get(outIndex).getIndex()).get("value");
                            outputEntries.get(targetColumnIndex.get(outIndex).getIndex()).put("value", targetColumnNewValue.get(outIndex));
                            oneRowResult.put("from", oldValue);
                            oneRowResult.put("to", targetColumnNewValue.get(outIndex));
                        }
                    }
                    queryResult.add(oneRowResult);
                }
                // Is there a Reason in the output? If yes, set it to the utterance value
                if (upsertResponseMap != null) {
                    try {
                        int idx = decisionUpdateContext.getOutputItems().indexOf("Reason");
                        if (idx > -1) {
                            String utterance = (String) upsertResponseMap.get("originalUtterance");
                            if (utterance != null && !utterance.isEmpty()) {
                                updateItemValue((Map) outputEntries.get(idx), utterance);
                            }
                        }
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }

            } // Condition met, bottom
//            System.out.printf("%d input(s), %d output, %d annotations\n",
//                    inputEntries.size(),
//                    outputEntries.size(),
//                    annotationEntries.size());
        });

        String jsonInString;
        if (queryResult.size() == 0) {
            if (verbose) {
                System.out.println(">> Warning: >> No update was done, inserting");
            }
            // Insert?
            if (rules.size() > 0) {
                if (upsertResponseMap != null) {
                    upsertResponseMap.put("upsertType", "insert");
                }
                // 1 - Clone the first row
                HashMap<String, Object> firstRule = (HashMap)rules.get(0);
                final Map<String, Object> newRule;

                // Serialize and De-serialize. Clone is NOT a deep copy.
                ByteArrayOutputStream bos = null;
                ByteArrayInputStream bis = null;
                try {
                    // Serialize
                    bos = new ByteArrayOutputStream();
                    ObjectOutputStream out = new ObjectOutputStream(bos);
                    out.writeObject(firstRule);
                    out.flush();
//                    byte[] byteArray = bos.toByteArray();
                    // De-Serialize
                    bis = new ByteArrayInputStream(bos.toByteArray());
                    ObjectInput in = new ObjectInputStream(bis);
                    newRule = (Map)in.readObject();
                } finally {
                    try {
                        if (bos != null) {
                            bos.close();
                        }
                        if (bis != null) {
                            bis.close();
                        }
                    } catch (IOException ex) {
                        // ignore close exception
                    }
                }
                rules.add(0, newRule); // Insert on top.
                // Now the rest!
                // 1 - Where Clause
                columnIndex.forEach(ci -> {
                    String newValue = whereColumnValue.get(columnIndex.indexOf(ci));
                    if (ci.getWhere() == DecisionContext.TargetColumnIndex.InOrOut.INPUT) {
                        Map<String, Object> inputEntry = (Map)((List)newRule.get("inputEntries")).get(ci.getIndex());
                        updateItemValue(inputEntry, newValue);
                    } else {
                        Map<String, Object> outputEntry = (Map)((List)newRule.get("outputEntries")).get(ci.getIndex());
                        updateItemValue(outputEntry, newValue);
                    }
                });
                // 2 - Update clause
                targetColumnIndex.forEach(tci -> {
                    int idx = targetColumnIndex.indexOf(tci);
                    String newValue = targetColumnNewValue.get(idx);
                    if (tci.getWhere() == DecisionContext.TargetColumnIndex.InOrOut.INPUT) {
                        Map<String, Object> inputEntry = (Map)((List)newRule.get("inputEntries")).get(tci.getIndex());
                        updateItemValue(inputEntry, newValue, decisionUpdateContext.rawTargetColumnValue.get(idx));
                    } else {
                        Map<String, Object> outputEntry = (Map)((List)newRule.get("outputEntries")).get(tci.getIndex());
                        updateItemValue(outputEntry, newValue, decisionUpdateContext.rawTargetColumnValue.get(idx));
                    }
                });
                // Is there a Reason in the output? If yes, set it to the utterance value
                if (upsertResponseMap != null) {
                    try {
                        int idx = decisionUpdateContext.getOutputItems().indexOf("Reason");
                        if (idx > -1) {
                            String utterance = (String) upsertResponseMap.get("originalUtterance");
                            if (utterance != null && !utterance.isEmpty()) {
                                List<Object> outputEntries = (List) newRule.get("outputEntries");
                                updateItemValue((Map) outputEntries.get(idx), utterance);
                            }
                        }
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            } else {
                System.out.println("Oops! No row to clone...");
            }
        } else {
            System.out.println("Update happened:" + mapper.writeValueAsString(queryResult));
        }
        jsonInString = mapper.writeValueAsString(jsonMap);
        return jsonInString;
    }

    static String processQuery(InputStream original, String userContext, String txSyntax) throws Exception {
        return processQuery(original, userContext, txSyntax, QueryOption.QUERY);
    }

    static String processQuery(InputStream original, String userContext, String querySyntax, QueryOption queryOption) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // THE Decision Table Object
        Map<String, Object> jsonMap = mapper.readValue(original, Map.class);
        // The User Context
        Map<String, Object> user = mapper.readValue(userContext, Map.class);
        // The query directive
        Map<String, Object> query = mapper.readValue(querySyntax, Map.class);
        // Set Update Context
        DecisionContext decisionQueryContext;
        if (queryOption == QueryOption.BAG_ENTITY) {
            decisionQueryContext = setBagEndContext(jsonMap, user, query);
        } else {
            decisionQueryContext = setQueryContext(jsonMap, user, query);
        }

        final List<String> columnId = decisionQueryContext.getWhereColumnId();
        final List<String> whereColumnValue = decisionQueryContext.getWhereColumnValue();

        // This is for an update. Not used for a select
        final List<String> targetColumnId = decisionQueryContext.getTargetColumnId();
        final List<String> targetColumnNewValue = decisionQueryContext.getTargetColumnValue();

        final List<DecisionContext.TargetColumnIndex> whereColumnIndex = decisionQueryContext.getWhereColumnIndex();
        final List<DecisionContext.TargetColumnIndex> targetColumnIndex = decisionQueryContext.getTargetColumnIndex();

//        if (columnId != null) { // Mean there IS a where
//            System.out.printf("[%s] found at index %d\n", columnId, columnIndex);
//        }
//        System.out.printf("[%s] found at index %d\n", targetColumnId, targetColumnIndex);

        final List<Object> queryResult = new ArrayList<>();

        // Find the rules
        List<Map<String, Object>> rules = (List)((Map)jsonMap.get("logic")).get("rules");
        rules.forEach(rule -> {
            List<Map<String, Object>> inputEntries = (List)rule.get("inputEntries");
            List<Map<String, Object>> outputEntries = (List)rule.get("outputEntries");
            List<Map<String, Object>> annotationEntries = (List)rule.get("annotationEntries");

            if (verbose) {
                System.out.println("-- Row " + rules.indexOf(rule) + " --");
            }
            boolean conditionMet = true;
            for (int ii=0; ii<whereColumnIndex.size(); ii++) { // Loop on the where clause elements
                DecisionContext.TargetColumnIndex colIdx = whereColumnIndex.get(ii);
                List<Object> values = (colIdx.getWhere() == DecisionContext.TargetColumnIndex.InOrOut.OUTPUT ?
                        Arrays.asList((String)outputEntries.get(colIdx.getIndex()).get("value")) :
                        getValues(inputEntries.get(colIdx.getIndex())));
                if (verbose) {
                    System.out.println("- Comparing column " +
                            "(" + colIdx.getIndex() + " " + colIdx.getWhere() + ") " +
                            decisionQueryContext.getWhereColumnId().get(ii) +
                            ", val " + values +
                            " and " + whereColumnValue.get(ii));
                }
                boolean met = (whereColumnValue.get(ii) == null || values.contains(whereColumnValue.get(ii)));
                conditionMet = conditionMet && met;
            }

            if (conditionMet) {
                Map<String, Object> oneRowResult = new HashMap<>();
                // Loop on the items to retrieve
                for (int outIndex = 0; outIndex < decisionQueryContext.targetColumnIndex.size(); outIndex++) {
                    // Column targetColumnId
                    int itemIndex = decisionQueryContext.targetColumnIndex.get(outIndex).getIndex();
                    String itemName = // columnId.get(itemIndex);
                            (decisionQueryContext.targetColumnIndex.get(outIndex).getWhere() == DecisionContext.TargetColumnIndex.InOrOut.INPUT) ?
                                    decisionQueryContext.getInputItems().get(itemIndex) :
                                    decisionQueryContext.getOutputItems().get(itemIndex);
                    if (decisionQueryContext.getOperation().get(outIndex) != null) {
                        if (decisionQueryContext.getOperation().get(outIndex).equals(DecisionContext.Operation.RANGE)) {
                            Map<String, Object> range = (Map) inputEntries.get(targetColumnIndex.get(outIndex).getIndex()).get("range");
                            if (range != null) {
                                Object endpoint1 = range.get("endpoint1");
                                if (verbose) {
                                    System.out.printf(">> Value for %s: currently %s\n", (whereColumnValue.get(outIndex) != null ? whereColumnValue.get(outIndex) : "this line"), endpoint1);
                                }
                                oneRowResult.put(itemName, endpoint1);
                            } else {
                                oneRowResult.put(itemName, "Range NotFound");
//                                throw new RuntimeException("No 'range' found where expected");
                            }
                        }
                    } else {
                        // System.out.println("Query with no function");
                        if (targetColumnIndex.get(outIndex).getWhere() == DecisionContext.TargetColumnIndex.InOrOut.OUTPUT) {
                            oneRowResult.put(itemName, outputEntries.get(targetColumnIndex.get(outIndex).getIndex()).get("value"));
                        } else {
                            Map<String, Object> itemObjectMap = inputEntries.get(targetColumnIndex.get(outIndex).getIndex());
                            if (itemObjectMap.get("range") != null) {
                                oneRowResult.put(itemName, itemObjectMap.get("range"));
                            } else if (itemObjectMap.get("values") != null) {
                                oneRowResult.put(itemName, itemObjectMap.get("values"));
                            } else if (itemObjectMap.get("value") != null) {
                                oneRowResult.put(itemName, itemObjectMap.get("value"));
                            } else {
                                oneRowResult.put(itemName, itemObjectMap);
                            }
                        }
                    }
                }
                queryResult.add(oneRowResult);
            }
//            System.out.printf("%d input(s), %d output, %d annotations\n",
//                    inputEntries.size(),
//                    outputEntries.size(),
//                    annotationEntries.size());
        });

        String jsonInString;
        if (queryResult.size() > 0) {
            jsonInString = mapper.writeValueAsString(queryResult); // .get(0));
        } else {
            jsonInString = "NOT_FOUND";
        }
        return jsonInString;
    }

    public static class DecisionContext {

        public enum Operation {
            RANGE("range"),
            APPEND_TO_LIST("appendToList"),      // Not for QUERY
            DELETE_FROM_LIST("deleteFromList");  // Not for QUERY

            private final String functionName;

            Operation(String functionName) {
                this.functionName = functionName;
            }
            public String functionName() {
                return this.functionName;
            }
        }

        public final static class TargetColumnIndex {
            enum InOrOut {
                INPUT,
                OUTPUT
            }
            int index;
            InOrOut where = InOrOut.INPUT; // Default

            public TargetColumnIndex(int idx) {
                this(idx, InOrOut.INPUT);
            }

            public TargetColumnIndex(int idx, InOrOut where) {
                this.index = idx;
                this.where = where;
            }

            public int getIndex() {
                return this.index;
            }

            public InOrOut getWhere() {
                return where;
            }
        }

        List<String> inputItems;
        List<String> outputItems;

        // From the where clause
        List<String> whereColumnId = new ArrayList<>();
        List<TargetColumnIndex> whereColumnIndex = new ArrayList<>();
        List<String> whereColumnValue = new ArrayList<>();
        // From the query or update clause
        List<String> targetColumnId = new ArrayList<>();
        List<TargetColumnIndex> targetColumnIndex = new ArrayList<>();
        List<String> targetColumnValue = new ArrayList<>();
        List<String> rawTargetColumnValue = new ArrayList<>();
        List<Operation> operation = new ArrayList<>();

        public DecisionContext() {}

        public List<String> getWhereColumnId() {
            return whereColumnId;
        }

        public void setWhereColumnId(List<String> whereColumnId) {
            this.whereColumnId = whereColumnId;
        }
        public void addWhereColumnId(String columnId) {
            this.whereColumnId.add(columnId);
        }

        public List<TargetColumnIndex> getWhereColumnIndex() {
            return whereColumnIndex;
        }

        public void setWhereColumnIndex(List<TargetColumnIndex> whereColumnIndex) {
            this.whereColumnIndex = whereColumnIndex;
        }
        public void addWhereColumnIndex(TargetColumnIndex whereColumnIndex) {
            this.whereColumnIndex.add(whereColumnIndex);
        }

        public List<String> getWhereColumnValue() {
            return whereColumnValue;
        }

        public void setWhereColumnValue(List<String> whereColumnValue) {
            this.whereColumnValue = whereColumnValue;
        }
        public void addWhereColumnValue(String whereColumnValue) {
            this.whereColumnValue.add(whereColumnValue);
        }

        public List<String> getTargetColumnId() {
            return targetColumnId;
        }

        public void setTargetColumnId(List<String> targetColumnId) {
            this.targetColumnId = targetColumnId;
        }

        public void addTargetColumnId(String targetColumnId) {
            this.targetColumnId.add(targetColumnId);
        }

        public List<TargetColumnIndex> getTargetColumnIndex() {
            return targetColumnIndex;
        }

        public void setTargetColumnIndex(List<TargetColumnIndex> targetColumnIndex) {
            this.targetColumnIndex = targetColumnIndex;
        }

        public void addTargetColumnIndex(TargetColumnIndex targetColumnIndex) {
            this.targetColumnIndex.add(targetColumnIndex);
        }

        public List<String> getTargetColumnValue() {
            return targetColumnValue;
        }

        public void setTargetColumnValue(List<String> targetColumnValue) {
            this.targetColumnValue = targetColumnValue;
        }

        public void addTargetColumnValue(String targetColumnValue) {
            this.targetColumnValue.add(targetColumnValue);
        }

        public List<Operation> getOperation() {
            return operation;
        }

        public void setOperation(List<Operation> operation) {
            this.operation = operation;
        }

        public void addOperation(Operation operation) {
            this.operation.add(operation);
        }

        public List<String> getRawTargetColumnValue() {
            return rawTargetColumnValue;
        }

        public void setRawTargetColumnValue(List<String> rawTargetColumnValue) {
            this.rawTargetColumnValue = rawTargetColumnValue;
        }

        public void addRawTargetColumnValue(String rawTargetColumnValue) {
            this.rawTargetColumnValue.add(rawTargetColumnValue);
        }

        public List<String> getInputItems() {
            return inputItems;
        }

        public void setInputItems(List<String> inputItems) {
            this.inputItems = inputItems;
        }

        public List<String> getOutputItems() {
            return outputItems;
        }

        public void setOutputItems(List<String> outputItems) {
            this.outputItems = outputItems;
        }

        protected static Operation detectOperation(String str) {
            Operation operation = null;

            for (Operation op : Operation.values()) {
                String patternStr = op.functionName() + "\\(.*\\)";
                Pattern pattern = Pattern.compile(patternStr);
                Matcher matcher = pattern.matcher(str);
                if (matcher.matches()) {
                    operation = op;
                    break;
                }
            }
            return operation;
        }

        /**
         * From 'range(350)', extract '350'
         * @param str
         * @param op
         * @return
         */
        protected static String extractFunctionParameter(String str, Operation op) {
            String extracted;
            String patternStr = op.functionName() + "\\(.*\\)";
            Pattern pattern = Pattern.compile(patternStr);
            Matcher matcher = pattern.matcher(str);
            if (matcher.matches()) {
                //                                                    '('                      ')'
                extracted = str.substring(op.functionName().length() + 1, str.trim().length() - 1);
            } else {
                throw new RuntimeException(String.format("[%s] does not match [%s]", str, op.functionName()));
            }
            return extracted;
        }
    }
}
