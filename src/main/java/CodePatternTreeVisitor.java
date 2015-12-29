import javax.lang.model.element.Name;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import com.sun.source.tree.*;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.JavacTask;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;

public class CodePatternTreeVisitor extends TreePathScanner<Void, Void> {
    // offsets of AST nodes in source file
    private final SourcePositions sourcePositions;
    private final Trees trees;
    // utility to operate on types
    private final Types types;
    private final TypeMirror mapType;
    private final Name getName;

    private CompilationUnitTree currCompUnit;

    CodePatternTreeVisitor(JavacTask task) {
        types = task.getTypes();
        trees = Trees.instance(task);
        sourcePositions = trees.getSourcePositions();

        // utility to operate on program elements
        Elements elements = task.getElements();
        // create the type element
        mapType = elements.getTypeElement("java.util.Map").asType();
        getName = elements.getName("get");
    }

    @Override
    public Void visitCompilationUnit(CompilationUnitTree tree, Void p) {
        currCompUnit = tree;
        return super.visitCompilationUnit(tree, p);
    }

    @Override
    public Void visitBinary(BinaryTree tree, Void p) {
        // unpack left and right hand side
        ExpressionTree left = tree.getLeftOperand();
        ExpressionTree right = tree.getRightOperand();
        Kind kind = tree.getKind();

        // apply our code pattern logic
        if (isGetCallOnMap(new TreePath(getCurrentPath(), left))
                && kind == Kind.EQUAL_TO
                && isNullLiteral(right)) {
            System.out.println("Found Match at line: "
                    + getLineNumber(tree) + " in "
                    + currCompUnit.getSourceFile().getName());
        }
        return super.visitBinary(tree, p);
    }

    private boolean isNullLiteral(ExpressionTree node) {
        // is this expression representing "null"?
        return (node.getKind() == Kind.NULL_LITERAL);
    }

    private boolean isGetCallOnMap(TreePath path) {
        switch (path.getLeaf().getKind()) {
            // is it a Method Invocation?
            case METHOD_INVOCATION:
                MethodInvocationTree methodInvocation = (MethodInvocationTree) path.getLeaf();
                // extract the identifier and receiver (methodSelectTree)
                ExpressionTree methodSelect = methodInvocation.getMethodSelect();
                switch (methodSelect.getKind()) {
                    case MEMBER_SELECT:
                        // extract receiver
                        MemberSelectTree mst = (MemberSelectTree) methodSelect;
                        ExpressionTree expr = mst.getExpression();
                        // get type of extracted receiver
                        TypeMirror type = trees.getTypeMirror(new TreePath(path, expr));
                        // extract method name
                        Name name = mst.getIdentifier();
                        // 1) check if receiver's type is subtype of java.util.Map
                        // 2) check if the extracted method name is exactly "get"
                        if (types.isAssignable(types.erasure(type), types.erasure(mapType))
                                && name == getName) {
                            return true;
                        }
                }
        }
        return false;
    }

    private long getLineNumber(Tree tree) {
        // map offsets to line numbers in source file
        LineMap lineMap = currCompUnit.getLineMap();
        if (lineMap == null)
            return -1;
        // find offset of the specified AST node
        long position = sourcePositions.getStartPosition(currCompUnit, tree);
        return lineMap.getLineNumber(position);
    }
}
