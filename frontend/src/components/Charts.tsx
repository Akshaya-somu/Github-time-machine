import {
  Area,
  AreaChart,
  CartesianGrid,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import { useMemo } from "react";

import type { RepositoryTimelineEntryDto } from "../types";

interface TrendChartProps {
  entries: RepositoryTimelineEntryDto[];
}

export function TrendChart({ entries }: TrendChartProps) {
  const data = useMemo(() => {
    const bucket = new Map<
      string,
      { date: string; additions: number; deletions: number; commits: number }
    >();
    const useYear = entries.length > 2_000;

    for (const entry of entries) {
      const date = new Date(entry.timestamp);
      const key = useYear
        ? `${date.getFullYear()}`
        : `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}`;
      const current = bucket.get(key) ?? {
        date: useYear
          ? key
          : date.toLocaleDateString(undefined, {
              month: "short",
              year: "numeric",
            }),
        additions: 0,
        deletions: 0,
        commits: 0,
      };
      current.additions += entry.additions;
      current.deletions += entry.deletions;
      current.commits += 1;
      bucket.set(key, current);
    }

    return [...bucket.values()];
  }, [entries]);

  return (
    <div className="chart-panel">
      <div className="panel-header">
        <h3>Additions vs Deletions</h3>
      </div>
      <div className="chart-wrapper">
        <ResponsiveContainer width="100%" height="100%">
          <AreaChart
            data={data}
            margin={{ top: 10, right: 20, left: 0, bottom: 0 }}
          >
            <defs>
              <linearGradient
                id="additionsGradient"
                x1="0"
                x2="0"
                y1="0"
                y2="1"
              >
                <stop offset="5%" stopColor="#3b82f6" stopOpacity={0.8} />
                <stop offset="95%" stopColor="#3b82f6" stopOpacity={0.1} />
              </linearGradient>
              <linearGradient
                id="deletionsGradient"
                x1="0"
                x2="0"
                y1="0"
                y2="1"
              >
                <stop offset="5%" stopColor="#f97316" stopOpacity={0.7} />
                <stop offset="95%" stopColor="#f97316" stopOpacity={0.1} />
              </linearGradient>
            </defs>
            <CartesianGrid stroke="rgba(148,163,184,0.2)" vertical={false} />
            <XAxis
              dataKey="date"
              tickLine={false}
              axisLine={false}
              tick={{ fill: "#94a3b8", fontSize: 12 }}
            />
            <YAxis
              tickLine={false}
              axisLine={false}
              tick={{ fill: "#94a3b8", fontSize: 12 }}
            />
            <Tooltip
              contentStyle={{
                backgroundColor: "#0f172a",
                border: "1px solid rgba(148,163,184,0.2)",
                borderRadius: "12px",
              }}
            />
            <Area
              type="monotone"
              dataKey="additions"
              stroke="#60a5fa"
              fill="url(#additionsGradient)"
              strokeWidth={2}
            />
            <Area
              type="monotone"
              dataKey="deletions"
              stroke="#f59e0b"
              fill="url(#deletionsGradient)"
              strokeWidth={2}
            />
          </AreaChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}
