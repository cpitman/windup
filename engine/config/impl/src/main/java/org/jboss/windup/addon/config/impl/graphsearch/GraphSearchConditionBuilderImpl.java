package org.jboss.windup.addon.config.impl.graphsearch;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.windup.addon.config.GraphRewrite;
import org.jboss.windup.addon.config.condition.GraphCondition;
import org.jboss.windup.addon.config.graphsearch.GraphSearchConditionBuilder;
import org.jboss.windup.addon.config.graphsearch.GraphSearchPropertyComparisonType;
import org.jboss.windup.addon.config.selectables.SelectionFactory;
import org.jboss.windup.graph.model.meta.WindupVertexFrame;
import org.ocpsoft.rewrite.context.EvaluationContext;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.FramedGraphQuery;

public class GraphSearchConditionBuilderImpl extends GraphCondition implements GraphSearchConditionBuilder
{
    private String variableName;

    private GraphSearchConditionBuilderImpl(String variableName)
    {
        this.variableName = variableName;
    }

    private List<GraphSearchCriterion> graphSearchCriteria = new ArrayList<>();

    public static GraphSearchConditionBuilder create(String collectionName)
    {
        return new GraphSearchConditionBuilderImpl(collectionName);
    }

    public GraphSearchConditionBuilder has(Class<? extends WindupVertexFrame> clazz)
    {
        graphSearchCriteria.add(new GraphSearchCriterionType(clazz));
        return this;
    }

    @Override
    public GraphSearchConditionBuilder withProperty(String property, String searchValue)
    {
        return withProperty(property, GraphSearchPropertyComparisonType.EQUALS, searchValue);
    }

    public GraphSearchConditionBuilder withProperty(String property, GraphSearchPropertyComparisonType searchType,
                String searchValue)
    {
        graphSearchCriteria.add(new GraphSearchCriterionProperty(property, searchType, searchValue));
        return this;
    }

    @Override
    public boolean evaluate(GraphRewrite event, EvaluationContext context)
    {
        FramedGraphQuery query = event.getGraphContext().getFramed().query();

        for (GraphSearchCriterion c : graphSearchCriteria)
        {
            c.query(query);
        }

        Set<WindupVertexFrame> frames = new HashSet<>();
        for (Vertex v : query.vertices())
        {
            WindupVertexFrame frame = event.getGraphContext().getFramed().frame(v, WindupVertexFrame.class);
            frames.add(frame);
        }

        SelectionFactory factory = (SelectionFactory) event.getRewriteContext().get(SelectionFactory.class);
        factory.push(frames, variableName);

        return !frames.isEmpty();
    }
}
