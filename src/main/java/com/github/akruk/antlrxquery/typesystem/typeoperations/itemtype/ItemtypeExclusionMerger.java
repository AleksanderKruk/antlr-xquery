package com.github.akruk.antlrxquery.typesystem.typeoperations.itemtype;

import java.util.HashSet;
import java.util.Set;

import com.github.akruk.antlrxquery.typesystem.defaults.XQueryItemType;
import com.github.akruk.antlrxquery.typesystem.factories.XQueryTypeFactory;

public class ItemtypeExclusionMerger extends ItemtypeBinaryOperation<XQueryItemType> {

    private XQueryTypeFactory typeFactory;

    ItemtypeExclusionMerger(XQueryTypeFactory typeFactory) {
        this.typeFactory = typeFactory;

    }


    @Override
    public XQueryItemType errorErrorOperation(XQueryItemType x, XQueryItemType y)
    {
        return x;
    }

    @Override
    public XQueryItemType errorAnyItemOperation(XQueryItemType x, XQueryItemType y)
    {
        return x;
    }

    @Override
    public XQueryItemType errorAnyNodeOperation(XQueryItemType x, XQueryItemType y)
    {
        return x;
    }

    @Override
    public XQueryItemType errorElementOperation(XQueryItemType x, XQueryItemType y)
    {
        return x;
    }

    @Override
    public XQueryItemType errorEnumOperation(XQueryItemType x, XQueryItemType y)
    {
        return x;
    }

    @Override
    public XQueryItemType errorBooleanOperation(XQueryItemType x, XQueryItemType y)
    {
        return x;
    }

    @Override
    public XQueryItemType errorNumberOperation(XQueryItemType x, XQueryItemType y)
    {
        return x;
    }

    @Override
    public XQueryItemType errorStringOperation(XQueryItemType x, XQueryItemType y)
    {
        return x;
    }

    @Override
    public XQueryItemType errorAnyMapOperation(XQueryItemType x, XQueryItemType y)
    {
        return x;
    }

    @Override
    public XQueryItemType errorMapOperation(XQueryItemType x, XQueryItemType y)
    {
        return x;
    }

    @Override
    public XQueryItemType errorChoiceOperation(XQueryItemType x, XQueryItemType y)
    {
        return x;
    }

    @Override
    public XQueryItemType errorAnyArrayOperation(XQueryItemType x, XQueryItemType y)
    {
                return x;
    }

    @Override
    public XQueryItemType errorArrayOperation(XQueryItemType x, XQueryItemType y)
    {
                return x;
    }

    @Override
    public XQueryItemType errorAnyFunctionOperation(XQueryItemType x, XQueryItemType y)
    {
                return x;
    }

    @Override
    public XQueryItemType errorFunctionOperation(XQueryItemType x, XQueryItemType y)
    {
                return x;
    }

    @Override
    public XQueryItemType errorRecordOperation(XQueryItemType x, XQueryItemType y)
    {
                return x;
    }

    @Override
    public XQueryItemType errorExtensibleRecordOperation(XQueryItemType x, XQueryItemType y)
    {
                return x;
    }

    @Override
    public XQueryItemType anyItemErrorOperation(XQueryItemType x, XQueryItemType y)
    {
                return x;
    }

    @Override
    public XQueryItemType anyItemAnyItemOperation(XQueryItemType x, XQueryItemType y)
    {
                return x;
    }

    @Override
    public XQueryItemType anyItemAnyNodeOperation(XQueryItemType x, XQueryItemType y)
    {
                return x;
    }

    @Override
    public XQueryItemType anyItemElementOperation(XQueryItemType x, XQueryItemType y)
    {
                return x;
    }

    @Override
    public XQueryItemType anyItemEnumOperation(XQueryItemType x, XQueryItemType y)
    {
                return x;
    }

    @Override
    public XQueryItemType anyItemBooleanOperation(XQueryItemType x, XQueryItemType y)
    {
                return x;
    }

    @Override
    public XQueryItemType anyItemNumberOperation(XQueryItemType x, XQueryItemType y)
    {
                return x;
    }

    @Override
    public XQueryItemType anyItemStringOperation(XQueryItemType x, XQueryItemType y)
    {
                return x;
    }

    @Override
    public XQueryItemType anyItemAnyMapOperation(XQueryItemType x, XQueryItemType y)
    {
                return x;
    }

    @Override
    public XQueryItemType anyItemMapOperation(XQueryItemType x, XQueryItemType y)
    {
                return x;
    }

    @Override
    public XQueryItemType anyItemChoiceOperation(XQueryItemType x, XQueryItemType y)
    {
                return x;
    }

    @Override
    public XQueryItemType anyItemAnyArrayOperation(XQueryItemType x, XQueryItemType y)
    {
                return x;
    }

    @Override
    public XQueryItemType anyItemArrayOperation(XQueryItemType x, XQueryItemType y)
    {
                return x;
    }

    @Override
    public XQueryItemType anyItemAnyFunctionOperation(XQueryItemType x, XQueryItemType y)
    {
                return x;
    }

    @Override
    public XQueryItemType anyItemFunctionOperation(XQueryItemType x, XQueryItemType y)
    {
                return x;
    }

    @Override
    public XQueryItemType anyItemRecordOperation(XQueryItemType x, XQueryItemType y)
    {
                return x;
    }

    @Override
    public XQueryItemType anyItemExtensibleRecordOperation(XQueryItemType x, XQueryItemType y)
    {
                return x;
    }

    @Override
    public XQueryItemType anyNodeErrorOperation(XQueryItemType x, XQueryItemType y)
    {
                return x;
    }

    @Override
    public XQueryItemType anyNodeAnyItemOperation(XQueryItemType x, XQueryItemType y)
    {
                return x;
    }

    @Override
    public XQueryItemType anyNodeAnyNodeOperation(XQueryItemType x, XQueryItemType y)
    {
                return x;
    }

    @Override
    public XQueryItemType anyNodeElementOperation(XQueryItemType x, XQueryItemType y)
    {
                return x;
    }

    @Override
    public XQueryItemType anyNodeEnumOperation(XQueryItemType x, XQueryItemType y)
    {
                return x;
    }

    @Override
    public XQueryItemType anyNodeBooleanOperation(XQueryItemType x, XQueryItemType y)
    {
                return x;
    }

    @Override
    public XQueryItemType anyNodeNumberOperation(XQueryItemType x, XQueryItemType y)
    {
                return x;
    }

    @Override
    public XQueryItemType anyNodeStringOperation(XQueryItemType x, XQueryItemType y)
    {
                return x;
    }

    @Override
    public XQueryItemType anyNodeAnyMapOperation(XQueryItemType x, XQueryItemType y)
    {
                return x;
    }

    @Override
    public XQueryItemType anyNodeMapOperation(XQueryItemType x, XQueryItemType y)
    {
                return x;
    }

    @Override
    public XQueryItemType anyNodeChoiceOperation(XQueryItemType x, XQueryItemType y)
    {
                return x;
    }

    @Override
    public XQueryItemType anyNodeAnyArrayOperation(XQueryItemType x, XQueryItemType y)
    {
                return x;
    }

    @Override
    public XQueryItemType anyNodeArrayOperation(XQueryItemType x, XQueryItemType y)
    {
                return x;
    }

    @Override
    public XQueryItemType anyNodeAnyFunctionOperation(XQueryItemType x, XQueryItemType y)
    {
                return x;
    }

    @Override
    public XQueryItemType anyNodeFunctionOperation(XQueryItemType x, XQueryItemType y)
    {
                return x;
    }

    @Override
    public XQueryItemType anyNodeRecordOperation(XQueryItemType x, XQueryItemType y)
    {
                return x;
    }

    @Override
    public XQueryItemType anyNodeExtensibleRecordOperation(XQueryItemType x, XQueryItemType y)
    {
                return x;
    }

    @Override
    public XQueryItemType elementErrorOperation(XQueryItemType x, XQueryItemType y)
    {
                return x;
    }

    @Override
    public XQueryItemType elementAnyItemOperation(XQueryItemType x, XQueryItemType y)
    {
                return x;
    }

    @Override
    public XQueryItemType elementAnyNodeOperation(XQueryItemType x, XQueryItemType y)
    {
                return x;
    }

    @Override
    public XQueryItemType elementElementOperation(XQueryItemType x, XQueryItemType y)
    {
        Set<String> elementNames = new HashSet<>(x.elementNames);
        if (elementNames.removeAll(y.elementNames))
            return typeFactory.itemElement(elementNames);
        return x;
    }

    @Override
    public XQueryItemType elementEnumOperation(XQueryItemType x, XQueryItemType y)
    {
                return x;
    }

    @Override
    public XQueryItemType elementBooleanOperation(XQueryItemType x, XQueryItemType y)
    {
                return x;
    }

    @Override
    public XQueryItemType elementNumberOperation(XQueryItemType x, XQueryItemType y)
    {
                return x;
    }

    @Override
    public XQueryItemType elementStringOperation(XQueryItemType x, XQueryItemType y)
    {
                return x;
    }

    @Override
    public XQueryItemType elementAnyMapOperation(XQueryItemType x, XQueryItemType y)
    {
                return x;
    }

    @Override
    public XQueryItemType elementMapOperation(XQueryItemType x, XQueryItemType y)
    {
                return x;
    }

    @Override
    public XQueryItemType elementChoiceOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'elementChoiceOperation'");
    }

    @Override
    public XQueryItemType elementAnyArrayOperation(XQueryItemType x, XQueryItemType y)
    {
                return x;
    }

    @Override
    public XQueryItemType elementArrayOperation(XQueryItemType x, XQueryItemType y)
    {
                return x;
    }

    @Override
    public XQueryItemType elementAnyFunctionOperation(XQueryItemType x, XQueryItemType y)
    {
                return x;
    }

    @Override
    public XQueryItemType elementFunctionOperation(XQueryItemType x, XQueryItemType y)
    {
                return x;
    }

    @Override
    public XQueryItemType elementRecordOperation(XQueryItemType x, XQueryItemType y)
    {
                return x;
    }

    @Override
    public XQueryItemType elementExtensibleRecordOperation(XQueryItemType x, XQueryItemType y)
    {
                return x;
    }

    @Override
    public XQueryItemType anyMapErrorOperation(XQueryItemType x, XQueryItemType y)
    {
                return x;
    }

    @Override
    public XQueryItemType anyMapAnyItemOperation(XQueryItemType x, XQueryItemType y)
    {
                return x;
    }

    @Override
    public XQueryItemType anyMapAnyNodeOperation(XQueryItemType x, XQueryItemType y)
    {
                return x;
    }

    @Override
    public XQueryItemType anyMapElementOperation(XQueryItemType x, XQueryItemType y)
    {
                return x;
    }

    @Override
    public XQueryItemType anyMapEnumOperation(XQueryItemType x, XQueryItemType y)
    {
                return x;
    }

    @Override
    public XQueryItemType anyMapBooleanOperation(XQueryItemType x, XQueryItemType y)
    {
                return x;
    }

    @Override
    public XQueryItemType anyMapNumberOperation(XQueryItemType x, XQueryItemType y)
    {
                return x;
    }

    @Override
    public XQueryItemType anyMapStringOperation(XQueryItemType x, XQueryItemType y)
    {
                return x;
    }

    @Override
    public XQueryItemType anyMapAnyMapOperation(XQueryItemType x, XQueryItemType y)
    {
        return x;
    }

    @Override
    public XQueryItemType anyMapMapOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'anyMapMapOperation'");
    }

    @Override
    public XQueryItemType anyMapChoiceOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'anyMapChoiceOperation'");
    }

    @Override
    public XQueryItemType anyMapAnyArrayOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType anyMapArrayOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType anyMapAnyFunctionOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType anyMapFunctionOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType anyMapRecordOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'anyMapRecordOperation'");
    }

    @Override
    public XQueryItemType anyMapExtensibleRecordOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'anyMapExtensibleRecordOperation'");
    }

    @Override
    public XQueryItemType mapErrorOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType mapAnyItemOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType mapAnyNodeOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType mapElementOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType mapEnumOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType mapBooleanOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType mapNumberOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType mapStringOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType mapAnyMapOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType mapMapOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'mapMapOperation'");
    }

    @Override
    public XQueryItemType mapChoiceOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'mapChoiceOperation'");
    }

    @Override
    public XQueryItemType mapAnyArrayOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType mapArrayOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType mapAnyFunctionOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType mapFunctionOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType mapRecordOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'mapRecordOperation'");
    }

    @Override
    public XQueryItemType mapExtensibleRecordOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'mapExtensibleRecordOperation'");
    }

    @Override
    public XQueryItemType anyArrayErrorOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType anyArrayAnyItemOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType anyArrayAnyNodeOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType anyArrayElementOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType anyArrayEnumOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType anyArrayBooleanOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType anyArrayNumberOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType anyArrayStringOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType anyArrayAnyMapOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType anyArrayMapOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType anyArrayChoiceOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'anyArrayChoiceOperation'");
    }

    @Override
    public XQueryItemType anyArrayAnyArrayOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType anyArrayArrayOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'anyArrayArrayOperation'");
    }

    @Override
    public XQueryItemType anyArrayAnyFunctionOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType anyArrayFunctionOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType anyArrayRecordOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType anyArrayExtensibleRecordOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType arrayErrorOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType arrayAnyItemOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType arrayAnyNodeOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType arrayElementOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType arrayEnumOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType arrayBooleanOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType arrayNumberOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType arrayStringOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType arrayAnyMapOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType arrayMapOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType arrayChoiceOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'arrayChoiceOperation'");
    }

    @Override
    public XQueryItemType arrayAnyArrayOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType arrayArrayOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'arrayArrayOperation'");
    }

    @Override
    public XQueryItemType arrayAnyFunctionOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType arrayFunctionOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType arrayRecordOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType arrayExtensibleRecordOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType anyFunctionErrorOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType anyFunctionAnyItemOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType anyFunctionAnyNodeOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType anyFunctionElementOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType anyFunctionEnumOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType anyFunctionBooleanOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType anyFunctionNumberOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType anyFunctionStringOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType anyFunctionAnyMapOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType anyFunctionMapOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType anyFunctionChoiceOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'anyFunctionChoiceOperation'");
    }

    @Override
    public XQueryItemType anyFunctionAnyArrayOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType anyFunctionArrayOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType anyFunctionAnyFunctionOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType anyFunctionFunctionOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'anyFunctionFunctionOperation'");
    }

    @Override
    public XQueryItemType anyFunctionRecordOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType anyFunctionExtensibleRecordOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType functionErrorOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType functionAnyItemOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType functionAnyNodeOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType functionElementOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType functionEnumOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType functionBooleanOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType functionNumberOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType functionStringOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType functionAnyMapOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType functionMapOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType functionChoiceOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'functionChoiceOperation'");
    }

    @Override
    public XQueryItemType functionAnyArrayOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType functionArrayOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType functionAnyFunctionOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'functionAnyFunctionOperation'");
    }

    @Override
    public XQueryItemType functionFunctionOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'functionFunctionOperation'");
    }

    @Override
    public XQueryItemType functionRecordOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType functionExtensibleRecordOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType enumErrorOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType enumAnyItemOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType enumAnyNodeOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType enumElementOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType enumEnumOperation(XQueryItemType x, XQueryItemType y)
    {
        Set<String> members = new HashSet<>(x.enumMembers);
        if (members.removeAll(y.enumMembers)) {
            return typeFactory.itemEnum(members);
        }
        return x;
    }

    @Override
    public XQueryItemType enumBooleanOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType enumNumberOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType enumStringOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType enumAnyMapOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType enumMapOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType enumChoiceOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'enumChoiceOperation'");
    }

    @Override
    public XQueryItemType enumAnyArrayOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType enumArrayOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType enumAnyFunctionOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType enumFunctionOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType enumRecordOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType enumExtensibleRecordOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType booleanErrorOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType booleanAnyItemOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType booleanAnyNodeOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType booleanElementOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType booleanEnumOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType booleanBooleanOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType booleanNumberOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType booleanStringOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType booleanAnyMapOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType booleanMapOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType booleanChoiceOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType booleanAnyArrayOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType booleanArrayOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType booleanAnyFunctionOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType booleanFunctionOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType booleanRecordOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType booleanExtensibleRecordOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType stringErrorOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType stringAnyItemOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType stringAnyNodeOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType stringElementOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType stringEnumOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType stringBooleanOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType stringNumberOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType stringStringOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType stringAnyMapOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType stringMapOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType stringChoiceOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType stringAnyArrayOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType stringArrayOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType stringAnyFunctionOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType stringFunctionOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType stringRecordOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType stringExtensibleRecordOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType numberErrorOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType numberAnyItemOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType numberAnyNodeOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType numberElementOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType numberEnumOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType numberBooleanOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType numberNumberOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType numberStringOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType numberAnyMapOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType numberMapOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType numberChoiceOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType numberAnyArrayOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType numberArrayOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType numberAnyFunctionOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType numberFunctionOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType numberRecordOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType numberExtensibleRecordOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        return x;
    }

    @Override
    public XQueryItemType choiceErrorOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'choiceErrorOperation'");
    }

    @Override
    public XQueryItemType choiceAnyItemOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'choiceAnyItemOperation'");
    }

    @Override
    public XQueryItemType choiceAnyNodeOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'choiceAnyNodeOperation'");
    }

    @Override
    public XQueryItemType choiceElementOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'choiceElementOperation'");
    }

    @Override
    public XQueryItemType choiceEnumOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'choiceEnumOperation'");
    }

    @Override
    public XQueryItemType choiceBooleanOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'choiceBooleanOperation'");
    }

    @Override
    public XQueryItemType choiceNumberOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'choiceNumberOperation'");
    }

    @Override
    public XQueryItemType choiceStringOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'choiceStringOperation'");
    }

    @Override
    public XQueryItemType choiceAnyMapOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'choiceAnyMapOperation'");
    }

    @Override
    public XQueryItemType choiceMapOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'choiceMapOperation'");
    }

    @Override
    public XQueryItemType choiceChoiceOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'choiceChoiceOperation'");
    }

    @Override
    public XQueryItemType choiceAnyArrayOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'choiceAnyArrayOperation'");
    }

    @Override
    public XQueryItemType choiceArrayOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'choiceArrayOperation'");
    }

    @Override
    public XQueryItemType choiceAnyFunctionOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'choiceAnyFunctionOperation'");
    }

    @Override
    public XQueryItemType choiceFunctionOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'choiceFunctionOperation'");
    }

    @Override
    public XQueryItemType choiceRecordOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'choiceRecordOperation'");
    }

    @Override
    public XQueryItemType choiceExtensibleRecordOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'choiceExtensibleRecordOperation'");
    }

    @Override
    public XQueryItemType recordErrorOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'recordErrorOperation'");
    }

    @Override
    public XQueryItemType recordAnyItemOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'recordAnyItemOperation'");
    }

    @Override
    public XQueryItemType recordAnyNodeOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'recordAnyNodeOperation'");
    }

    @Override
    public XQueryItemType recordElementOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'recordElementOperation'");
    }

    @Override
    public XQueryItemType recordEnumOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'recordEnumOperation'");
    }

    @Override
    public XQueryItemType recordBooleanOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'recordBooleanOperation'");
    }

    @Override
    public XQueryItemType recordNumberOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'recordNumberOperation'");
    }

    @Override
    public XQueryItemType recordStringOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'recordStringOperation'");
    }

    @Override
    public XQueryItemType recordAnyMapOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'recordAnyMapOperation'");
    }

    @Override
    public XQueryItemType recordMapOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'recordMapOperation'");
    }

    @Override
    public XQueryItemType recordChoiceOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'recordChoiceOperation'");
    }

    @Override
    public XQueryItemType recordAnyArrayOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'recordAnyArrayOperation'");
    }

    @Override
    public XQueryItemType recordArrayOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'recordArrayOperation'");
    }

    @Override
    public XQueryItemType recordAnyFunctionOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'recordAnyFunctionOperation'");
    }

    @Override
    public XQueryItemType recordFunctionOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'recordFunctionOperation'");
    }

    @Override
    public XQueryItemType recordRecordOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'recordRecordOperation'");
    }

    @Override
    public XQueryItemType recordExtensibleRecordOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'recordExtensibleRecordOperation'");
    }

    @Override
    public XQueryItemType extensibleRecordErrorOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'extensibleRecordErrorOperation'");
    }

    @Override
    public XQueryItemType extensibleRecordAnyItemOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'extensibleRecordAnyItemOperation'");
    }

    @Override
    public XQueryItemType extensibleRecordAnyNodeOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'extensibleRecordAnyNodeOperation'");
    }

    @Override
    public XQueryItemType extensibleRecordElementOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'extensibleRecordElementOperation'");
    }

    @Override
    public XQueryItemType extensibleRecordEnumOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'extensibleRecordEnumOperation'");
    }

    @Override
    public XQueryItemType extensibleRecordBooleanOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'extensibleRecordBooleanOperation'");
    }

    @Override
    public XQueryItemType extensibleRecordNumberOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'extensibleRecordNumberOperation'");
    }

    @Override
    public XQueryItemType extensibleRecordStringOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'extensibleRecordStringOperation'");
    }

    @Override
    public XQueryItemType extensibleRecordAnyMapOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'extensibleRecordAnyMapOperation'");
    }

    @Override
    public XQueryItemType extensibleRecordMapOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'extensibleRecordMapOperation'");
    }

    @Override
    public XQueryItemType extensibleRecordChoiceOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'extensibleRecordChoiceOperation'");
    }

    @Override
    public XQueryItemType extensibleRecordAnyArrayOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'extensibleRecordAnyArrayOperation'");
    }

    @Override
    public XQueryItemType extensibleRecordArrayOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'extensibleRecordArrayOperation'");
    }

    @Override
    public XQueryItemType extensibleRecordAnyFunctionOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'extensibleRecordAnyFunctionOperation'");
    }

    @Override
    public XQueryItemType extensibleRecordFunctionOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'extensibleRecordFunctionOperation'");
    }

    @Override
    public XQueryItemType extensibleRecordRecordOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'extensibleRecordRecordOperation'");
    }

    @Override
    public XQueryItemType extensibleRecordExtensibleRecordOperation(XQueryItemType x, XQueryItemType y)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'extensibleRecordExtensibleRecordOperation'");
    }


}
