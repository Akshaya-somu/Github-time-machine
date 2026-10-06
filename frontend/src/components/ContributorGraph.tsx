import {
  Background,
  Controls,
  ReactFlow,
  type Edge,
  type Node,
} from "@xyflow/react";
import { useMemo, useState } from "react";
import "@xyflow/react/dist/style.css";

import type { ContributorDto, ContributorGraphDto } from "../types";

interface ContributorGraphProps {
  data: ContributorGraphDto;
  contributors?: ContributorDto[];
  isLargeRepository?: boolean;
  onSelectContributor?: (email: string) => void;
}

export function ContributorGraph({
  data,
  contributors = [],
  isLargeRepository = false,
  onSelectContributor,
}: ContributorGraphProps) {
  const [search, setSearch] = useState("");
  const [visibleCount, setVisibleCount] = useState(50);
  const normalizedSearch = search.trim().toLowerCase();
  const matchingContributors = useMemo(
    () =>
      contributors.filter((contributor) =>
        `${contributor.name} ${contributor.email}`
          .toLowerCase()
          .includes(normalizedSearch),
      ),
    [contributors, normalizedSearch],
  );
  const visibleContributors = matchingContributors.slice(0, visibleCount);
  const visibleIds = new Set(
    visibleContributors.map((contributor) => contributor.id),
  );
  const visibleGraphNodes = data.nodes
    .filter((node) => visibleIds.size === 0 || visibleIds.has(node.id))
    .slice(0, 50);
  const visibleNodeIds = new Set(visibleGraphNodes.map((node) => node.id));

  const nodes: Node[] = visibleGraphNodes.map((node, index) => {
    const radius = Math.max(72, Math.min(140, node.commits * 18));
    return {
      id: node.id,
      type: "default",
      position: {
        x: 120 + (index % 2) * 260,
        y: 60 + Math.floor(index / 2) * 180,
      },
      data: {
        label: node.name || node.email,
        commits: node.commits,
        email: node.email,
      },
      style: {
        width: radius,
        height: radius * 0.72,
        borderRadius: 16,
        background: "rgba(15, 23, 42, 0.9)",
        border: "1px solid rgba(96,165,250,0.8)",
        color: "#e2e8f0",
        boxShadow: "0 10px 30px rgba(15, 23, 42, 0.25)",
      },
    };
  });

  const edges: Edge[] = data.edges
    .filter(
      (edge) =>
        visibleNodeIds.has(edge.source) && visibleNodeIds.has(edge.target),
    )
    .slice(0, 200)
    .map((edge) => ({
      id: `${edge.source}-${edge.target}`,
      source: edge.source,
      target: edge.target,
      animated: false,
      type: "smoothstep",
      style: {
        stroke: "#60a5fa",
        strokeWidth: Math.max(1.5, edge.weight),
      },
      label: `${edge.sharedFiles} shared`,
      labelStyle: { fill: "#cbd5e1", fontSize: 11 },
    }));

  return (
    <div className="graph-panel">
      <div className="panel-header">
        <h3>Contributor Collaboration</h3>
        <span className="panel-kicker">
          Showing {visibleContributors.length.toLocaleString()} of{" "}
          {contributors.length.toLocaleString()} contributors
        </span>
      </div>
      <div className="graph-toolbar">
        <input
          className="search-input"
          value={search}
          onChange={(event) => {
            setSearch(event.target.value);
            setVisibleCount(50);
          }}
          placeholder="Search contributors"
        />
        {isLargeRepository ? (
          <span className="panel-kicker">Graph limited for performance</span>
        ) : null}
      </div>
      <div className="graph-wrapper">
        {isLargeRepository && nodes.length === 0 ? (
          <div className="empty-state">
            Search for a contributor to load a focused collaboration
            neighborhood.
          </div>
        ) : nodes.length === 0 ? (
          <div className="empty-state">
            No contributor collaboration data yet.
          </div>
        ) : (
          <ReactFlow
            nodes={nodes}
            edges={edges}
            fitView
            minZoom={0.4}
            maxZoom={1.8}
            onNodeClick={(_, node) => onSelectContributor?.(String(node.id))}
            defaultEdgeOptions={{ type: "smoothstep" }}
          >
            <Background color="#1e293b" gap={18} />
            <Controls />
          </ReactFlow>
        )}
      </div>
      {visibleContributors.length < matchingContributors.length ? (
        <button
          type="button"
          className="secondary-button load-more-button"
          onClick={() => setVisibleCount((count) => count + 50)}
        >
          Load more contributors
        </button>
      ) : null}
      <div className="contributor-directory">
        {visibleContributors.map((contributor) => (
          <button
            key={contributor.id}
            type="button"
            className="directory-item"
            onClick={() => onSelectContributor?.(contributor.email)}
          >
            <span>{contributor.name || contributor.email}</span>
            <strong>{contributor.commitCount}</strong>
          </button>
        ))}
      </div>
    </div>
  );
}
