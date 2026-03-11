using DrawMap.Domain;

namespace DrawMap.Repositories;

public interface IRouteRepository
{
    Task<string> AddRoute(Route route);
    Task<Route?> GetRoute(string routeId);
    Task<Route?> UpdateRoute(string routeId, Route route);
    Task<bool> DeleteRoute(string routeId);
}
