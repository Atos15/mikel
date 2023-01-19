package me.astral.mal.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public sealed class MALControlStatement permits MALIfStatement, MALMultiWayBranchStatement, MALGotoStatement {

}
