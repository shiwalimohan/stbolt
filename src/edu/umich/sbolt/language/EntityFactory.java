package edu.umich.sbolt.language;

public class EntityFactory
{
    public static LinguisticEntity createEntity(String entityType){
        if(entityType.equals(GoalInfo.TYPE)){
            return new GoalInfo();
        } else if(entityType.equals(LingObject.TYPE)){
            return new LingObject();
        } else if(entityType.equals(ObjectRelation.TYPE)){
            return new ObjectRelation();
        } else if(entityType.equals(ProposalInfo.TYPE)){
            return new ProposalInfo();
        } else if(entityType.equals(Sentence.TYPE)){
            return new Sentence();
        } else if(entityType.equals(VerbCommand.TYPE)){
            return new VerbCommand();
        } else if(entityType.equals(ObjectIdentification.TYPE)){
            return new ObjectIdentification();
        } else {
            return null;
        }
    }
}
