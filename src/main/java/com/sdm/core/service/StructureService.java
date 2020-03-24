package com.sdm.core.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sdm.Constants;
import com.sdm.core.model.ModelInfo;
import com.sdm.core.model.annotation.Filterable;
import com.sdm.core.model.annotation.Grid;
import com.sdm.core.util.Globalizer;
import com.sdm.file.model.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

@Service
public class StructureService {

    public static final Logger logger = LoggerFactory.getLogger(StructureService.class);

    /**
     * Check Field need to Process
     *
     * @param field
     * @return
     */
    private boolean checkField(Field field) {
        if(field.isAnnotationPresent(Transient.class) || field.isAnnotationPresent(JsonIgnore.class)){
            return false;
        }

        return (field.isAnnotationPresent(Column.class) || field.isAnnotationPresent(Id.class) ||
                field.isAnnotationPresent(ManyToOne.class) || field.isAnnotationPresent(ManyToMany.class) ||
                field.isAnnotationPresent(OneToOne.class) || field.isAnnotationPresent(OneToMany.class) ||
                field.isAnnotationPresent(ElementCollection.class) || field.isAnnotationPresent(Embedded.class));
    }

    private boolean isModel(String packageName) {
        return packageName.matches("com.sdm.([a-z]+\\.)*model");
    }

    private void loadGeneralInfo(Field field, ModelInfo model) {
        model.setName(field.getName());
        model.setDataType(field.getType().getSimpleName());
        model.setLabel(Globalizer.camelToReadable(field.getName()));
        model.getGrid().setLabel(Globalizer.camelToReadable(field.getName()));
        model.getGrid().setAlignment(ModelInfo.Alignment.center);
        model.getGrid().setType(ModelInfo.GridType.text);

        if (model.getDataType().matches("[sS]tring|[cC]har")) {
            model.setType("text");
            model.getGrid().setAlignment(ModelInfo.Alignment.left);
            model.getGrid().setSortable(true);
        } else if (model.getDataType().matches("([iI]nt.*)|[lL]ong|[dD]ecimal|[dD]ouble|[fF]loat|[sS]hort")) {
            model.setType("number");
            model.getGrid().setAlignment(ModelInfo.Alignment.right);
            model.getGrid().setSortable(true);
        } else if (model.getDataType().equalsIgnoreCase("date")) {
            Temporal temporal = field.getAnnotation(Temporal.class);
            if (temporal != null) {
                if (temporal.value() == TemporalType.TIMESTAMP) {
                    model.setType("datetime");
                } else {
                    model.setType(temporal.value().toString().toLowerCase());
                }
            }
            model.getGrid().setSortable(true);
        } else if (field.getType().getPackageName().equals("java.util")) {
            ParameterizedType wrapperType = (ParameterizedType) field.getGenericType();
            if (wrapperType.getActualTypeArguments().length == 1) {
                Class<?> innerType = (Class<?>) wrapperType.getActualTypeArguments()[0];
                if (isModel(innerType.getPackageName())) {
                    model.setDataType(innerType.getName() + "[]");
                    model.setType("table");
                } else {
                    model.setDataType(innerType.getSimpleName() + "[]");
                }
                model.addExtra("multi", true);
            } else if (wrapperType.getActualTypeArguments().length == 2) {
                Class<?> keyType = (Class<?>) wrapperType.getActualTypeArguments()[0];
                Class<?> valueType = (Class<?>) wrapperType.getActualTypeArguments()[1];
                String dataType = keyType.getSimpleName() + ":";
                dataType += isModel(valueType.getPackageName()) ? valueType.getName() : valueType.getSimpleName();
                model.setDataType(dataType);
                model.setType("raw");
                model.addExtra("dictionary", true);
            } else {
                model.setType("raw");
                model.addExtra("innerType", wrapperType.getActualTypeArguments());
            }
        } else if (isModel(field.getType().getPackageName())) {
            model.setDataType(field.getType().getName());
            if (field.getType().isEnum()) {
                model.setType("list");
                model.addExtra("data", field.getType().getEnumConstants());
            } else if (field.getType().isAssignableFrom(File.class)) {
                model.setType("file");
                model.getGrid().setType(ModelInfo.GridType.image);
            } else {
                model.setType("table");
            }
        } else if (model.getDataType().equalsIgnoreCase("boolean")) {
            model.setType("checkbox");
            model.getGrid().setType(ModelInfo.GridType.bool);
            model.getGrid().setSortable(true);
        }


        if (field.isAnnotationPresent(Id.class)) {
            model.setPrimaryKey(true);
            model.setRequired(true);
            model.getGrid().setLabel("#");
        }

        if (field.isAnnotationPresent(Column.class)) {
            Column column = field.getAnnotation(Column.class);
            if (field.getType() == String.class && model.getMax() <= 0) {
                model.setMax(column.length());
            }

            if (!column.nullable()) {
                model.setRequired(true);
            }
        }

        if (field.isAnnotationPresent(Filterable.class)) {
           model.getGrid().setFilterable(true);
        }

        /**
         * Grid Info
         */
        if(field.isAnnotationPresent(Grid.class)){
            Grid grid = field.getAnnotation(Grid.class);
            if(grid.hide()){
                model.setGrid(null);
            }else{
                model.getGrid().setLabel(grid.value());
                model.getGrid().setAlignment(grid.alignment());
                model.getGrid().setSortable(grid.sortable());
                model.getGrid().setMinWidth(grid.minWidth());
                model.getGrid().setType(grid.type());
                model.getGrid().setOnLoad(grid.onLoad());
            }
        }
    }

    private void loadCondition(Field field, ModelInfo model) {
        for (Annotation annotation : field.getAnnotations()) {
            if (annotation.annotationType() == Size.class) {
                Size size = (Size) annotation;
                if (size.min() > 0) {
                    model.setMin(size.min());
                }
                if (model.getMax() <= 0) {
                    model.setMax(size.max());
                }
            } else if (annotation.annotationType() == Email.class) {
                model.setType("email");
                model.addExtra("pattern", Constants.Pattern.EMAIL);
            } else if (annotation.annotationType() == Pattern.class) {
                Pattern pattern = (Pattern) annotation;
                model.addExtra("pattern", pattern.regexp());
            } else if (annotation.annotationType().getPackageName().equals("javax.validation.constraints")) {
                model.addExtra(annotation.annotationType().getSimpleName(), annotation);
            }
        }
    }

    public List<ModelInfo> buildStructure(Class<?> entityClass) {
        List<ModelInfo> structure = new ArrayList<>();
        for (Field field : entityClass.getDeclaredFields()) {
            if (!checkField(field)) {
                continue;
            }
            ModelInfo model = new ModelInfo();

            try {
                this.loadGeneralInfo(field, model);
                this.loadCondition(field, model);
            } catch (Exception ex) {

            }

            structure.add(model);
        }
        return structure;
    }
}
